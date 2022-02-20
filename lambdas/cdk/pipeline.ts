#!/usr/bin/env node
import * as codebuild from '@aws-cdk/aws-codebuild';
import * as iam from '@aws-cdk/aws-iam';
import * as cdk from '@aws-cdk/core';

import { StackProps } from './cdk';

export class CodebuildStack extends cdk.Stack {
    constructor(scope: cdk.Construct, id: string, props: StackProps) {
      super(scope, id);
  
      const codebuildProjectName = `plp-beachist-${props.stage}-build`;
      const codebuildServiceRoleName = `role-${codebuildProjectName}`;
  
      const codebuildServiceRole = new iam.Role(this, 'CodeBuildRole', {
        roleName: codebuildServiceRoleName,
        assumedBy: new iam.ServicePrincipal('codebuild.amazonaws.com'),
        managedPolicies: [
          iam.ManagedPolicy.fromAwsManagedPolicyName('CloudWatchFullAccess'),
          iam.ManagedPolicy.fromAwsManagedPolicyName('AWSLambda_FullAccess'),
          iam.ManagedPolicy.fromAwsManagedPolicyName('IAMFullAccess'),
          iam.ManagedPolicy.fromAwsManagedPolicyName('AWSCloudFormationFullAccess'),
          iam.ManagedPolicy.fromAwsManagedPolicyName('AWSIoTFullAccess'),
          new iam.ManagedPolicy(this, 'PipelineManagedPolicy', {
            managedPolicyName: 'pol-beachist-pipeline-managed-policy',
            statements: [
              new iam.PolicyStatement({
                actions: ['codepipeline:*'],
                resources: ['*'],
              }),
              new iam.PolicyStatement({
                actions: ['ssm:GetParameter', 'ssm:GetParameters'],
                resources: [`arn:aws:ssm:${this.region}:${this.account}:parameter*`],
              }),
              new iam.PolicyStatement({
                actions: ['apigateway:*'],
                resources: ['arn:aws:apigateway:*::/*'],
              }),
              new iam.PolicyStatement({
                actions: ['codebuild:*'],
                resources: ['*'],
              }),
              new iam.PolicyStatement({
                actions: ['kms:*'],
                resources: ['*'],
              }),
              new iam.PolicyStatement({
                actions: ['s3:*'],
                resources: ['*'],
              }),
            ],
          }),
        ],
      });
  
      new codebuild.Project(this, 'CodeBuildProject', {
        projectName: codebuildProjectName,
        buildSpec: codebuild.BuildSpec.fromSourceFilename('buildspec.yml'),
        environmentVariables: {
          STAGE: {
            type: codebuild.BuildEnvironmentVariableType.PLAINTEXT,
            value: props.stage,
          },
        },
        environment: {
          buildImage: codebuild.LinuxBuildImage.STANDARD_5_0,
          privileged: true,
        },
        source: codebuild.Source.gitHub({
          owner: 'dlrgsbz',
          repo: 'beachist',
          branchOrRef: 'main',
        }),
        role: codebuildServiceRole,
      });
    }
  }
  