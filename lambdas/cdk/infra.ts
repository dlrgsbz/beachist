import * as fs from 'fs'
import * as path from 'path'

import { AwsCustomResource, AwsCustomResourcePolicy, PhysicalResourceId } from 'aws-cdk-lib/custom-resources'
import { Effect, PolicyDocument, PolicyStatement } from 'aws-cdk-lib/aws-iam'
import { Rule as EventRule, Schedule } from 'aws-cdk-lib/aws-events'
import { IotRepublishMqttAction, LambdaFunctionAction, MqttQualityOfService } from '@aws-cdk/aws-iot-actions-alpha'
import { IotSql, TopicRule } from '@aws-cdk/aws-iot-alpha'
import { Function as LambdaFunction, Runtime, RuntimeFamily } from 'aws-cdk-lib/aws-lambda'
import { Stage, Timeouts, environmentProps } from './config'

import { CfnPolicy } from 'aws-cdk-lib/aws-iot'
import { Construct } from 'constructs'
import { LambdaFunction as LambdaFunctionTarget } from 'aws-cdk-lib/aws-events-targets'
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

  private readonly nodeVersion: string

  private readonly lambdaEnvs: LambdaEnvs

  private readonly getThingShadowRoleArn: string

  constructor(scope: Construct, id: string, props: StackProps) {
    super(scope, id)
    this.props = props

    this.nodeVersion = fs.readFileSync(path.join(__dirname, '../', '.nvmrc'), 'utf-8').replace('\n', '');

    this.getThingShadowRoleArn = `'arn:aws:iam::${this.account}:role/get-thing-shadow'`

    const iotPolicy = this.createIotPolicy()

    this.lambdaEnvs = {
      STAGE: environmentProps.stage,
      BACKEND_URL: environmentProps.backendUrl,
      IOT_DATA_ENDPOINT: this.getIotEndpoint(),
      LOG_LEVEL: environmentProps.logLevel,
      AWS_ACCOUNT_ID: Stack.of(this).account,
      IOT_POLICY_NAME: iotPolicy.policyName!,
    }

    this.createGetFieldsHandler()
    this.createCreateEntryHandler()
    this.createCreateEventHandler()
    this.createCreateSpecialEventHandler()
    this.createProvisioningHandler()
    this.createInfoHandler()
    this.createUpdateCrewHandler()
    this.createLastWillTopic()
    this.createGetUviHandler()
    this.createGetWeatherHandler()
    this.createGetWaterTempHandler()
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

  private createUpdateCrewHandler() {
    const updateCrewHandler = this.lambdaFunction(
      `${this.props.prefix}-update-crew`,
      path.join('iot-triggers', 'update-crew'),
      `updateCrewHandler`,
      this.lambdaEnvs,
    )

    new TopicRule(this, 'update-crew-rule', {
      topicRuleName: `beachist${this.props.stage}UpdateCrewRule`,
      sql: IotSql.fromStringAsVer20160323(
        `SELECT
            crew, date, 
            get_thing_shadow(topic(1), ${this.getThingShadowRoleArn}).state.desired.stationId AS stationId,
          FROM '+/crew'`,
      ),
      actions: [new LambdaFunctionAction(updateCrewHandler)],
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

  private createCreateSpecialEventHandler() {
    const createSpecialEventHandler = this.lambdaFunction(
      `${this.props.prefix}-create-special-event`,
      path.join('iot-triggers', 'create-special-event'),
      `createSpecialEventHandler`,
      this.lambdaEnvs,
    )

    addIotPublishToTopicRole(createSpecialEventHandler)

    new TopicRule(this, 'create-special-event-rule', {
      topicRuleName: `beachist${this.props.stage}CreateSpecialEventRule`,
      sql: IotSql.fromStringAsVer20160323(
        `SELECT 
            date, note, title, kind, notifier, id,
            topic(1) AS iotThingName, 
            get_thing_shadow(topic(1), ${this.getThingShadowRoleArn}).state.desired.stationId AS stationId
          FROM '+/special-event'`,
      ),
      actions: [new LambdaFunctionAction(createSpecialEventHandler)],
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

  private createGetUviHandler() {
    const uviHandler = this.lambdaFunction(
        `${this.props.prefix}-uvi`,
        path.join('cron', 'get-uvi'),
        `getUviHandler`,
        this.lambdaEnvs,
    )

    addIotPublishToTopicRole(uviHandler)
    addSsmReadRole(uviHandler)

    if (this.props.stage === Stage.PROD) {
      const rulePrefix = `${this.props.prefix}-uvi`;
      const hour = '7-15' // UTC
      this.addSeasonCronRule(rulePrefix, uviHandler, hour);
    }
  }

  private createGetWeatherHandler() {
    const weatherHandler = this.lambdaFunction(
        `${this.props.prefix}-weather`,
        path.join('cron', 'get-weather'),
        `getWeatherHandler`,
        this.lambdaEnvs,
    )

    addIotPublishToTopicRole(weatherHandler)
    addSsmReadRole(weatherHandler)

    if (this.props.stage === Stage.PROD) {
      const rulePrefix = `${this.props.prefix}-weather`;
      const hour = '7-15' // UTC
      const minute = '55'
      this.addSeasonCronRule(rulePrefix, weatherHandler, hour, minute);
    }
  }

  private createGetWaterTempHandler() {
    const waterTempHandler = this.lambdaFunction(
        `${this.props.prefix}-water-temp`,
        path.join('cron', 'get-water-temp'),
        `getWaterTempHandler`,
        this.lambdaEnvs,
    )

    addIotPublishToTopicRole(waterTempHandler)
    addSsmReadRole(waterTempHandler)

    if (this.props.stage === Stage.PROD) {
      const rulePrefix = `${this.props.prefix}-water`;
      const hour = '7' // UTC
      const minute = '15'
      this.addSeasonCronRule(rulePrefix, waterTempHandler, hour, minute);
    }
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
                current.state.reported.connected      AS connected,
                topic(3) AS iotThingName,
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
              `arn:aws:iot:${Stack.of(this).region}:${Stack.of(this).account}:topic/$aws/things/\${iot:ClientId}/*`,
              `arn:aws:iot:${Stack.of(this).region}:${Stack.of(this).account}:topic/\${iot:ClientId}/*`,
            ],
          }),
          new PolicyStatement({
            effect: Effect.ALLOW,
            actions: ['iot:Receive'],
            resources: [
              `arn:aws:iot:${Stack.of(this).region}:${Stack.of(this).account}:topic/shared/*`,
            ],
          }),
          new PolicyStatement({
            effect: Effect.ALLOW,
            actions: ['iot:Subscribe'],
            resources: [
              `arn:aws:iot:${Stack.of(this).region}:${Stack.of(this).account}:topicfilter/$aws/things/\${iot:ClientId}/*`,
              `arn:aws:iot:${Stack.of(this).region}:${Stack.of(this).account}:topicfilter/\${iot:ClientId}/*`,
              `arn:aws:iot:${Stack.of(this).region}:${Stack.of(this).account}:topicfilter/shared/*`,
            ],
          }),
        ],
      }),
    })
  }

  private addSeasonCronRule(rulePrefix: string, f: LambdaFunction, hour: string, minute = '30') {
    const mainRule = new EventRule(this, `${rulePrefix}-main-rule`, {
      schedule: Schedule.cron({
        month: '6,7,8',
        hour,
        minute,
      }),
    });
    mainRule.addTarget(new LambdaFunctionTarget(f))
    const preRule = new EventRule(this, `${rulePrefix}-pre-rule`, {
      schedule: Schedule.cron({
        day: '15-31',
        month: '5',
        hour,
        minute,
      }),
    });
    preRule.addTarget(new LambdaFunctionTarget(f))
    const postRule = new EventRule(this, `${rulePrefix}-post-rule`, {
      schedule: Schedule.cron({
        day: '1-15',
        month: '9',
        hour,
        minute,
      }),
    });
    postRule.addTarget(new LambdaFunctionTarget(f))
  }

  private lambdaFunction(name: string, dir: string, handler: string, environment: LambdaEnvs): LambdaFunction {
    return new NodejsFunction(this, name, {
      functionName: name,
      entry: path.join(__dirname, '../src/', dir, 'index.ts'),
      runtime: new Runtime(`nodejs${this.nodeVersion}.x`, RuntimeFamily.NODEJS),
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

  private getIotEndpoint(): string {
    const resource = new AwsCustomResource(this, 'IoTEndpoint', {
      onCreate: {
          service: 'Iot',
          action: 'describeEndpoint',
          physicalResourceId: PhysicalResourceId.fromResponse('endpointAddress'),
          parameters: {
            endpointType: "iot:Data-ATS"
          }
      },
      policy: AwsCustomResourcePolicy.fromSdkCalls({resources: AwsCustomResourcePolicy.ANY_RESOURCE})
    });

    return resource.getResponseField('endpointAddress')
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

const addSsmReadRole = (f: LambdaFunction) => {
  f.addToRolePolicy(
      new PolicyStatement({
        actions: ['ssm:GetParameter', 'ssm:GetParameters'],
        resources: ['*'],
        effect: Effect.ALLOW,
      }),
  )
}
