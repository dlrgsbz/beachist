#!/usr/bin/env node

import { BuildEnvironmentVariableType, BuildSpec, LinuxBuildImage, Project, Source } from 'aws-cdk-lib/aws-codebuild';
import { ManagedPolicy, PolicyStatement, Role, ServicePrincipal } from 'aws-cdk-lib/aws-iam';

import { Construct } from 'constructs';
import { Stack } from 'aws-cdk-lib';
import { StackProps } from './cdk';

export class CodebuildStack extends Stack {
    constructor(scope: Construct, id: string, props: StackProps) {
      super(scope, id);
  
      const codebuildProjectName = `plp-beachist-${props.stage}-build`;
      const codebuildServiceRoleName = `role-${codebuildProjectName}`;
  
      const codebuildServiceRole = new Role(this, 'CodeBuildRole', {
        roleName: codebuildServiceRoleName,
        assumedBy: new ServicePrincipal('codebuild.amazonaws.com'),
        managedPolicies: [
          ManagedPolicy.fromAwsManagedPolicyName('CloudWatchFullAccess'),
          ManagedPolicy.fromAwsManagedPolicyName('AWSLambda_FullAccess'),
          ManagedPolicy.fromAwsManagedPolicyName('IAMFullAccess'),
          ManagedPolicy.fromAwsManagedPolicyName('AWSCloudFormationFullAccess'),
          ManagedPolicy.fromAwsManagedPolicyName('AWSIoTFullAccess'),
          new ManagedPolicy(this, 'PipelineManagedPolicy', {
            managedPolicyName: `pol-beachist-${props.stage}-pipeline-managed-policy`,
            statements: [
              new PolicyStatement({
                actions: ['codepipeline:*'],
                resources: ['*'],
              }),
              new PolicyStatement({
                actions: ['ssm:GetParameter', 'ssm:GetParameters'],
                resources: [`arn:aws:ssm:${this.region}:${this.account}:parameter*`],
              }),
              new PolicyStatement({
                actions: ['apigateway:*'],
                resources: ['arn:aws:apigateway:*::/*'],
              }),
              new PolicyStatement({
                actions: ['codebuild:*'],
                resources: ['*'],
              }),
              new PolicyStatement({
                actions: ['kms:*'],
                resources: ['*'],
              }),
              new PolicyStatement({
                actions: ['s3:*'],
                resources: ['*'],
              }),
            ],
          }),
        ],
      });
  
      new Project(this, 'CodeBuildProject', {
        projectName: codebuildProjectName,
        buildSpec: BuildSpec.fromSourceFilename('buildspec.yml'),
        environmentVariables: {
          STAGE: {
            type: BuildEnvironmentVariableType.PLAINTEXT,
            value: props.stage,
          },
        },
        environment: {
          buildImage: LinuxBuildImage.STANDARD_5_0,
          privileged: true,
        },
        source: Source.gitHub({
          owner: 'dlrgsbz',
          repo: 'beachist',
          branchOrRef: 'main',
        }),
        role: codebuildServiceRole,
      });
    }
  }
  