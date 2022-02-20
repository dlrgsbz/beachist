import * as path from 'path'
import { Construct, Stack } from "@aws-cdk/core"
import { IotSql, TopicRule } from '@aws-cdk/aws-iot'
import { Code, Runtime, Function as LambdaFunction } from '@aws-cdk/aws-lambda'
import { StackProps } from "./cdk"
import * as iam from '@aws-cdk/aws-iam';
import { environmentProps, Stage, Timeouts } from './config'
import { IotRepublishMqttAction, LambdaFunctionAction, MqttQualityOfService } from '@aws-cdk/aws-iot-actions';

interface LambdaEnvs {
    STAGE: Stage
    BACKEND_URL: string
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

        this.lambdaEnvs = {
            STAGE: environmentProps.stage,
            BACKEND_URL: environmentProps.backendUrl,
        }

        this.createCreateEntryHandler(handlerPrefix, props)
        this.createLastWillTopic(props)
    }

    private createLastWillTopic(props: StackProps) {
        /*
{
    "rule": {
    "ruleDisabled": false,
    "sql": "SELECT * FROM 'my/things/myLightBulb/update'",
    "description": "Turn my/things/ into $aws/things/",
    "actions": [
        {
        "republish": {
            "topic": "$$aws/things/myLightBulb/shadow/update",
            "roleArn": "arn:aws:iam:123456789012:role/aws_iot_republish"
            }
        }
     ]
   }
}
        */
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

    private createCreateEntryHandler(handlerPrefix: string, props: StackProps) {
        const createEntryHandler = this.lambdaFunction(
            `${props.prefix}-create-entry`,
            `${handlerPrefix}createEntryHandler`,
            this.lambdaEnvs,
        );

        addIotPublishToTopic(createEntryHandler);

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

function addIotShadowUpdatePermission(f: LambdaFunction) {
    f.addToRolePolicy(
        new iam.PolicyStatement({
            actions: ['iot:UpdateThingShadow'],
            resources: ['*'],
            effect: iam.Effect.ALLOW,
        }),
    );
}

function addIotPublishToTopic(f: LambdaFunction) {
    f.addToRolePolicy(
        new iam.PolicyStatement({
            actions: ['iot:Publish', 'iot:Connect'],
            resources: ['*'],
            effect: iam.Effect.ALLOW,
        }),
    );
}
