#!/usr/bin/env node

import { App } from 'aws-cdk-lib';
import { InfraStack } from './infra';
import { Stage } from './config';

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

    new InfraStack(app, stackProps.prefix, stackProps);
};

setup().catch((err) => {
    // eslint-disable-next-line no-console
    console.error(err);
    process.exit(1);
});
