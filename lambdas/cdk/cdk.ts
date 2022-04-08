#!/usr/bin/env node
import { App } from 'aws-cdk-lib';
import { Stage } from './config';
import { InfraStack } from './infra';
import { CodebuildStack } from './pipeline';

const app = new App();

export interface StackProps {
    readonly serviceName: string
    readonly prefix: string
    readonly stage: Stage
}

// eslint-disable-next-line @typescript-eslint/require-await
const setup = async () => {
    const stage = app.node.tryGetContext('stage') as Stage | undefined;

    if (!stage) {
        throw new Error('Failed to determine stage, try cdk synth -c stage=dev');
    }

    const stackProps: StackProps = {
        serviceName: 'beachist',
        prefix: `beachist-${stage}`,
        stage,
    };

    new CodebuildStack(app, `${stackProps.prefix}-pipeline`, stackProps);
    new InfraStack(app, stackProps.prefix, stackProps);
};

setup().catch((err) => {
    // eslint-disable-next-line no-console
    console.error(err);
    process.exit(1);
});
