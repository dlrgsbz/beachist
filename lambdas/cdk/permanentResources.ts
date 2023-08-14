import { AttributeType, BillingMode, ITable, Table, TableEncryption } from 'aws-cdk-lib/aws-dynamodb'
import { RemovalPolicy, Stack } from 'aws-cdk-lib'

import { Construct } from 'constructs'
import { StackProps } from './cdk'

export class PermanentResourcesStack extends Stack {
  private readonly table: Table

  get beachistTable(): ITable {
    return this.table
  }

  constructor(scope: Construct, id: string, private readonly props: StackProps) {
    super(scope, id)

    this.table = new Table(this, `${props.prefix}-dynamo-table`, {
      tableName: `${props.prefix}-dynamo-table`,
      partitionKey: {
        name: 'pk',
        type: AttributeType.STRING,
      },
      sortKey: {
        name: 'sk',
        type: AttributeType.STRING,
      },
      pointInTimeRecovery: true,
      billingMode: BillingMode.PAY_PER_REQUEST,
      removalPolicy: RemovalPolicy.RETAIN,
      encryption: TableEncryption.DEFAULT,
    })

    this.table.addGlobalSecondaryIndex({
      indexName: 'station-id-index',
      partitionKey: {
        name: 'sk',
        type: AttributeType.STRING,
      },
      sortKey: {
        name: 'stationId',
        type: AttributeType.STRING,
      },
    })
  }
}
