import * as path from 'path'
import { StackProps } from "./cdk"
import { environmentProps, Stage, Timeouts } from './config'
import { Stack } from 'aws-cdk-lib'
import { Construct } from 'constructs'
import { Code, Runtime } from 'aws-cdk-lib/aws-lambda'
import { IotSql, TopicRule } from '@aws-cdk/aws-iot-alpha'
import { IotRepublishMqttAction, LambdaFunctionAction, MqttQualityOfService } from '@aws-cdk/aws-iot-actions-alpha'
import { Function as LambdaFunction } from 'aws-cdk-lib/aws-lambda'
import { Effect, PolicyDocument, PolicyStatement } from 'aws-cdk-lib/aws-iam'
import { CfnPolicy } from 'aws-cdk-lib/aws-iot'

interface LambdaEnvs {
  STAGE: Stage
  BACKEND_URL: string
  IOT_DATA_ENDPOINT: string
  LOG_LEVEL: 'debug' | 'info' | 'warn' | 'error'
  AWS_ACCOUNT_ID: string
  IOT_POLICY_NAME: string
}

export class InfraStack extends Stack {
  private readonly code: Code

  private readonly lambdaEnvs: LambdaEnvs

  private readonly getThingShadowRoleArn: string

  constructor(scope: Construct, id: string, props: StackProps) {
    super(scope, id)
    const handlerPrefix = 'dist/index.'

    this.code = Code.fromAsset(path.join(__dirname, '../dist/index.js.zip'))

    this.getThingShadowRoleArn = `'arn:aws:iam::${this.account}:role/get-thing-shadow'`

    const iotPolicy = this.createIotPolicy(props)

    this.lambdaEnvs = {
      STAGE: environmentProps.stage,
      BACKEND_URL: environmentProps.backendUrl,
      IOT_DATA_ENDPOINT: environmentProps.awsConfig.iotDataEndpoint,
      LOG_LEVEL: environmentProps.logLevel,
      AWS_ACCOUNT_ID: Stack.of(this).account,
      IOT_POLICY_NAME: iotPolicy.policyName!,
    }

    this.createGetFieldsHandler(handlerPrefix, props)
    this.createCreateEntryHandler(handlerPrefix, props)
    this.createCreateEventHandler(handlerPrefix, props)
    this.createProvisioningHandler(handlerPrefix, props)
    this.createLastWillTopic(props)
  }

  private createLastWillTopic(props: StackProps) {
    new TopicRule(this, 'update-shadow-last-will-rule', {
      topicRuleName: `beachist${props.stage}UpdateShadowLastWillRule`,
      sql: IotSql.fromStringAsVer20160323(
        `SELECT * FROM '+/connected/update'`
      ),
      actions: [new IotRepublishMqttAction(
        '$$aws/things/${topic(1)}/shadow/update',
        { qualityOfService: MqttQualityOfService.AT_LEAST_ONCE }
      )]
    })
  }

  private createGetFieldsHandler(handlerPrefix: string, props: StackProps) {
    const getFieldsHandler = this.lambdaFunction(
      `${props.prefix}-get-fields`,
      `${handlerPrefix}getFieldsHandler`,
      this.lambdaEnvs,
    )

    addIotPublishToTopicRole(getFieldsHandler)

    new TopicRule(this, 'get-fields-rule', {
      topicRuleName: `beachist${props.stage}GetFieldsRule`,
      sql: IotSql.fromStringAsVer20160323(
        `SELECT
            topic(1) as iotThingName,
            get_thing_shadow(topic(1), ${this.getThingShadowRoleArn}).state.desired.stationId AS stationId,
          FROM '+/field/get'`,
      ),
      actions: [new LambdaFunctionAction(getFieldsHandler)],
    })
  }

  private createCreateEventHandler(handlerPrefix: string, props: StackProps) {
    const createEventHandler = this.lambdaFunction(
      `${props.prefix}-create-event`,
      `${handlerPrefix}createEventHandler`,
      this.lambdaEnvs,
    )

    addIotPublishToTopicRole(createEventHandler)

    new TopicRule(this, 'create-event-rule', {
      topicRuleName: `beachist${props.stage}CreateEventRule`,
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

  private createCreateEntryHandler(handlerPrefix: string, props: StackProps) {
    const createEntryHandler = this.lambdaFunction(
      `${props.prefix}-create-entry`,
      `${handlerPrefix}createEntryHandler`,
      this.lambdaEnvs,
    );

    addIotPublishToTopicRole(createEntryHandler)

    new TopicRule(this, 'create-entry-rule', {
      topicRuleName: `beachist${props.stage}CreateEntryRule`,
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
    });
  }

  private createProvisioningHandler(handlerPrefix: string, props: StackProps) {
    const f = this.lambdaFunction(
      `${props.prefix}-provisioning`,
      `${handlerPrefix}provisioningHandler`,
      this.lambdaEnvs
    )

    addIotShadowUpdateRole(f)

    f.addToRolePolicy(new PolicyStatement({
      actions: [
        "iot:AttachPolicy",
        "iot:AttachThingPrincipal",
        "iot:AddThingToThingGroup",
        "iot:CreateKeysAndCertificate",
        "iot:CreatePolicy",
        "iot:CreateThing",
        "iot:CreateThingGroup",
        "iot:CreateThingType",
        "iot:DeleteCertificate",
        "iot:DescribeEndpoint",
        "iot:DescribeThing",
        "iot:DescribeThingGroup",
        "iot:DetachPolicy",
        "iot:DetachThingPrincipal",
        "iot:ListAttachedPolicies",
        "iot:ListThings",
        "iot:ListThingGroups",
        "iot:ListThingsInThingGroup",
        "iot:ListThingPrincipals",
        "iot:UpdateCertificate",
      ],
      effect: Effect.ALLOW,
      resources: ['*'],
    }))

    // todo: add function URL
  }

  private createIotPolicy(props: StackProps): CfnPolicy {
    return new CfnPolicy(this, `${props.prefix}-iot-policy`, {
      policyName: `${props.prefix}-iot-policy`,
      policyDocument: new PolicyDocument({
        statements: [
          new PolicyStatement({
            effect: Effect.ALLOW,
            actions: ['iot:UpdateThingShadow', 'iot:UpdateThingShadow'],
            resources: [`arn:aws:iot:${Stack.of(this).region}:${Stack.of(this).account}:thing/\${iot:ClientId}`]
          }),
          new PolicyStatement({
            effect: Effect.ALLOW,
            actions: ['iot:Publish', 'iot:Receive', 'iot:Subscribe'],
            resources: [
              `arn:aws:iot:${Stack.of(this).region}:${Stack.of(this).account}:topicfilter/$aws/things/\${iot:ClientId}/*`,
              `arn:aws:iot:${Stack.of(this).region}:${Stack.of(this).account}:topicfilter/\${iot:ClientId}/*`,
            ],
          }),
        ]
      })
    })
  }

  lambdaFunction(name: string, handler: string, environment: LambdaEnvs): LambdaFunction {
    return new LambdaFunction(this, name, {
      functionName: name,
      runtime: Runtime.NODEJS_14_X,
      code: this.code,
      handler,
      memorySize: 1024,
      timeout: Timeouts.lambdaTimeout,
      environment: environment as unknown as Record<string, string>,
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
  );
}

const addIotPublishToTopicRole = (f: LambdaFunction) => {
  f.addToRolePolicy(
    new PolicyStatement({
      actions: ['iot:Publish', 'iot:Connect'],
      resources: ['*'],
      effect: Effect.ALLOW,
    }),
  );
}
