import * as path from 'path'

import { Effect, PolicyDocument, PolicyStatement } from 'aws-cdk-lib/aws-iam'
import { IotRepublishMqttAction, LambdaFunctionAction, MqttQualityOfService } from '@aws-cdk/aws-iot-actions-alpha'
import { IotSql, TopicRule } from '@aws-cdk/aws-iot-alpha'
import { Function as LambdaFunction, Runtime } from 'aws-cdk-lib/aws-lambda'
import { Stage, Timeouts, environmentProps } from './config'

import { CfnPolicy } from 'aws-cdk-lib/aws-iot'
import { Construct } from 'constructs'
import { NodejsFunction } from 'aws-cdk-lib/aws-lambda-nodejs'
import { Stack } from 'aws-cdk-lib'
import { StackProps } from './cdk'

interface LambdaEnvs {
  STAGE: Stage
  BACKEND_URL: string
  IOT_DATA_ENDPOINT: string
  LOG_LEVEL: 'debug' | 'info' | 'warn' | 'error'
  AWS_ACCOUNT_ID: string
  IOT_POLICY_NAME: string
}

export class InfraStack extends Stack {
  private readonly props: StackProps

  private readonly lambdaEnvs: LambdaEnvs

  private readonly getThingShadowRoleArn: string

  constructor(scope: Construct, id: string, props: StackProps) {
    super(scope, id)
    this.props = props

    this.getThingShadowRoleArn = `'arn:aws:iam::${this.account}:role/get-thing-shadow'`

    const iotPolicy = this.createIotPolicy()

    this.lambdaEnvs = {
      STAGE: environmentProps.stage,
      BACKEND_URL: environmentProps.backendUrl,
      IOT_DATA_ENDPOINT: environmentProps.awsConfig.iotDataEndpoint,
      LOG_LEVEL: environmentProps.logLevel,
      AWS_ACCOUNT_ID: Stack.of(this).account,
      IOT_POLICY_NAME: iotPolicy.policyName!,
    }

    this.createGetFieldsHandler()
    this.createCreateEntryHandler()
    this.createCreateEventHandler()
    this.createProvisioningHandler()
    this.createInfoHandler()
    this.createLastWillTopic()
  }

  private createLastWillTopic() {
    new TopicRule(this, 'update-shadow-last-will-rule', {
      topicRuleName: `beachist${this.props.stage}UpdateShadowLastWillRule`,
      sql: IotSql.fromStringAsVer20160323(
        `SELECT * FROM '+/connected/update'`,
      ),
      actions: [new IotRepublishMqttAction(
        '$$aws/things/${topic(1)}/shadow/update',
        { qualityOfService: MqttQualityOfService.AT_LEAST_ONCE },
      )],
    })
  }

  private createGetFieldsHandler() {
    const getFieldsHandler = this.lambdaFunction(
      `${this.props.prefix}-get-fields`,
      path.join('iot-triggers', 'get-fields'),
      `getFieldsHandler`,
      this.lambdaEnvs,
    )

    addIotPublishToTopicRole(getFieldsHandler)

    new TopicRule(this, 'get-fields-rule', {
      topicRuleName: `beachist${this.props.stage}GetFieldsRule`,
      sql: IotSql.fromStringAsVer20160323(
        `SELECT
            topic(1) as iotThingName,
            get_thing_shadow(topic(1), ${this.getThingShadowRoleArn}).state.desired.stationId AS stationId,
          FROM '+/field/get'`,
      ),
      actions: [new LambdaFunctionAction(getFieldsHandler)],
    })
  }

  private createCreateEventHandler() {
    const createEventHandler = this.lambdaFunction(
      `${this.props.prefix}-create-event`,
      path.join('iot-triggers', 'create-event'),
      `createEventHandler`,
      this.lambdaEnvs,
    )

    addIotPublishToTopicRole(createEventHandler)

    new TopicRule(this, 'create-event-rule', {
      topicRuleName: `beachist${this.props.stage}CreateEventRule`,
      sql: IotSql.fromStringAsVer20160323(
        `SELECT 
            id, type, date, 
            topic(1) AS iotThingName, 
            get_thing_shadow(topic(1), ${this.getThingShadowRoleArn}).state.desired.stationId AS stationId,
          FROM '+/event'`,
      ),
      actions: [new LambdaFunctionAction(createEventHandler)],
    })
  }

  private createCreateEntryHandler() {
    const createEntryHandler = this.lambdaFunction(
      `${this.props.prefix}-create-entry`,
      path.join('iot-triggers', 'create-entry'),
      `createEntryHandler`,
      this.lambdaEnvs,
    )

    addIotPublishToTopicRole(createEntryHandler)

    new TopicRule(this, 'create-entry-rule', {
      topicRuleName: `beachist${this.props.stage}CreateEntryRule`,
      sql: IotSql.fromStringAsVer20160323(
        `SELECT 
                    state, 
                    stateKind, 
                    amount, 
                    note, 
                    crew, 
                    topic(1) AS iotThingName, 
                    topic(3) AS fieldId,
                    get_thing_shadow(topic(1), ${this.getThingShadowRoleArn}).state.desired.stationId AS stationId,
                  FROM '+/field/+/entry'`,
      ),
      actions: [new LambdaFunctionAction(createEntryHandler)],
    })
  }

  private createInfoHandler() {
    const appInfoUpdatedHandler = this.lambdaFunction(
      `${this.props.prefix}-app-info-updated`,
      path.join('iot-triggers', 'app-info-updated'),
      `appInfoUpdatedHandler`,
      this.lambdaEnvs,
    )

    addIotPublishToTopicRole(appInfoUpdatedHandler)

    return new TopicRule(this, `${this.props.prefix}-app-info-rule`, {
      topicRuleName: `beachist${this.props.stage}AppInfoTopicRule`,
      sql: IotSql.fromStringAsVer20160323(
        `SELECT current.state.reported.appVersionCode AS appVersionCode,
                current.state.reported.appVersion     AS appVersion,
                current.state.desired.stationId       AS stationId,
                current.state.reported.connected      AS connected
         FROM '$aws/things/+/shadow/update/documents'
         WHERE (isUndefined(previous.state.reported.appVersionCode) AND NOT isUndefined(current.state.reported.appVersionCode)) 
         OR previous.state.reported.appVersionCode <> current.state.reported.appVersionCode
         OR (isUndefined(previous.state.reported.connected) AND NOT isUndefined(current.state.reported.connected))
         OR previous.state.reported.connected <> current.state.reported.connected`,
      ),
      actions: [new LambdaFunctionAction(appInfoUpdatedHandler)],
    })
  }

  private createProvisioningHandler() {
    const f = this.lambdaFunction(
      `${this.props.prefix}-provisioning`,
      path.join('http', 'provisioning'),
      `provisioningHandler`,
      this.lambdaEnvs,
    )

    addIotShadowUpdateRole(f)

    f.addToRolePolicy(new PolicyStatement({
      actions: [
        'iot:AttachPolicy',
        'iot:AttachThingPrincipal',
        'iot:AttachPrincipalPolicy',
        'iot:AddThingToThingGroup',
        'iot:CreateKeysAndCertificate',
        'iot:CreatePolicy',
        'iot:CreateThing',
        'iot:CreateThingGroup',
        'iot:CreateThingType',
        'iot:DeleteCertificate',
        'iot:DescribeEndpoint',
        'iot:DescribeThing',
        'iot:DescribeThingGroup',
        'iot:DetachPolicy',
        'iot:DetachThingPrincipal',
        'iot:ListAttachedPolicies',
        'iot:ListThings',
        'iot:ListThingGroups',
        'iot:ListThingsInThingGroup',
        'iot:ListThingPrincipals',
        'iot:UpdateCertificate',
      ],
      effect: Effect.ALLOW,
      resources: ['*'],
    }))

    // todo: add function URL
  }

  private createIotPolicy(): CfnPolicy {
    return new CfnPolicy(this, `${this.props.prefix}-iot-policy`, {
      policyName: `${this.props.prefix}-iot-policy`,
      policyDocument: new PolicyDocument({
        statements: [
          // todo: this should not be here
          new PolicyStatement({
            effect: Effect.ALLOW,
            actions: ['iot:*'],
            resources: ['*'],
          }),
          new PolicyStatement({
            effect: Effect.ALLOW,
            actions: ['iot:GetThingShadow'],
            resources: [`arn:aws:iot:${Stack.of(this).region}:${Stack.of(this).account}:thing/\${iot:ClientId}`],
          }),
          new PolicyStatement({
            effect: Effect.ALLOW,
            actions: ['iot:UpdateThingShadow'],
            resources: [`arn:aws:iot:${Stack.of(this).region}:${Stack.of(this).account}:thing/\${iot:ClientId}`],
          }),
          new PolicyStatement({
            effect: Effect.ALLOW,
            actions: ['iot:Publish', 'iot:Receive'],
            resources: [
              `arn:aws:iot:${Stack.of(this).region}:${Stack.of(this).account}:topicfilter/$aws/things/\${iot:ClientId}/*`,
              `arn:aws:iot:${Stack.of(this).region}:${Stack.of(this).account}:topicfilter/\${iot:ClientId}/*`,
            ],
          }),
          new PolicyStatement({
            effect: Effect.ALLOW,
            actions: ['iot:Subscribe'],
            resources: [
              `arn:aws:iot:${Stack.of(this).region}:${Stack.of(this).account}:topicfilter/$aws/things/\${iot:ClientId}/*`,
              `arn:aws:iot:${Stack.of(this).region}:${Stack.of(this).account}:topicfilter/\${iot:ClientId}/*`,
            ],
          }),
        ],
      }),
    })
  }

  private lambdaFunction(name: string, dir: string, handler: string, environment: LambdaEnvs): LambdaFunction {
    return new NodejsFunction(this, name, {
      functionName: name,
      entry: path.join(__dirname, '../src/', dir, 'index.ts'),
      runtime: Runtime.NODEJS_14_X,
      handler,
      memorySize: 1024,
      timeout: Timeouts.lambdaTimeout,
      environment: environment as unknown as Record<string, string>,
      bundling: {
        minify: true,
        sourceMap: this.props.stage === Stage.DEV,
      },
    })
  }
}

const addIotShadowUpdateRole = (f: LambdaFunction) => {
  f.addToRolePolicy(
    new PolicyStatement({
      actions: ['iot:UpdateThingShadow'],
      resources: ['*'],
      effect: Effect.ALLOW,
    }),
  )
}

const addIotPublishToTopicRole = (f: LambdaFunction) => {
  f.addToRolePolicy(
    new PolicyStatement({
      actions: ['iot:Publish', 'iot:RetainPublish', 'iot:Connect'],
      resources: ['*'],
      effect: Effect.ALLOW,
    }),
  )
}
