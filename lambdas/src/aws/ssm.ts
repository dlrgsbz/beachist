import {
    SSMClient as AWSSSMClient,
    GetParameterCommand,
    GetParameterCommandInput,
    GetParameterCommandOutput,
    GetParametersCommand,
    GetParametersCommandInput,
    GetParametersCommandOutput,
} from '@aws-sdk/client-ssm'

export interface SSMClient {
    getParameter: (name: string) => Promise<string>
    getParameters: (names: string[]) => Promise<Record<string, string>>
}

const awsSsmClient = new AWSSSMClient()

class SSMClientImpl implements SSMClient {
    client: AWSSSMClient

    constructor(client: AWSSSMClient = awsSsmClient) {
        this.client = client
    }

    async getParameters(names: string[]): Promise<Record<string, string>> {
        const params: GetParametersCommandInput = {
            Names: names,
            WithDecryption: true,
        }
        const command = new GetParametersCommand(params)

        const response: GetParametersCommandOutput = await this.client.send(command)
        if (!response.Parameters) {
            throw Error(`Error getting parameters from SSM`);
        }

        return response.Parameters?.reduce((acc, parameter) => {
            if (parameter.Name && parameter.Value) {
                acc[parameter.Name] = parameter.Value
            }
            return acc
        }, {} as Record<string, string>)
    }

    async getParameter(name: string): Promise<string> {
        const params: GetParameterCommandInput = {
            Name: name,
            WithDecryption: true,
        };
        const command = new GetParameterCommand(params);

        const response: GetParameterCommandOutput = await this.client.send(command);
        if (response.Parameter?.Value) {
            return response.Parameter.Value;
        }
        throw Error(`Error getting parameter ${name} from SSM`);
    }
}

export const ssmClient: SSMClient = new SSMClientImpl()
