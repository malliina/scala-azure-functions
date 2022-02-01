param location string = resourceGroup().location
param uniqueId string = uniqueString(resourceGroup().id)

resource appServicePlan 'Microsoft.Web/serverfarms@2021-02-01' = {
  name: 'plan-${uniqueId}'
  location: location
  kind: 'functionapp'
  sku: {
    name: 'Y1'
    tier: 'Dynamic'
  }
}

resource storageAccount 'Microsoft.Storage/storageAccounts@2021-02-01' = {
  name: 'storage${uniqueId}'
  location: location
  kind: 'StorageV2'
  sku: {
    name: 'Standard_LRS'
  }
  properties: {
    supportsHttpsTrafficOnly: true
    encryption: {
      services: {
        file: {
          keyType: 'Account'
          enabled: true
        }
        blob: {
          keyType: 'Account'
          enabled: true
        }
      }
      keySource: 'Microsoft.Storage'
    }
    accessTier: 'Hot'
  }
}

resource logAnalyticsWorkspace 'Microsoft.OperationalInsights/workspaces@2020-10-01' = {
  name: 'workspace-${uniqueId}'
  location: location
  properties: {
    sku: {
      name: 'PerGB2018'
    }
  }
}

resource appInsightsComponents 'Microsoft.Insights/components@2020-02-02-preview' = {
  name: 'insights-${uniqueId}'
  location: location
  kind: 'web'
  properties: {
    Application_Type: 'web'
    WorkspaceResourceId: logAnalyticsWorkspace.id
    publicNetworkAccessForIngestion: 'Enabled'
    publicNetworkAccessForQuery: 'Enabled'
  }
}

resource azureFunction 'Microsoft.Web/sites@2020-12-01' = {
  name: 'app-${uniqueId}'
  location: location
  kind: 'functionapp'
  properties: {
    serverFarmId: appServicePlan.id
    httpsOnly: true
    reserved: false
    siteConfig: {
      appSettings: [
        {
          name: 'AzureWebJobsStorage'
          value: 'DefaultEndpointsProtocol=https;AccountName=${storageAccount.name};AccountKey=${storageAccount.listKeys().keys[0].value}'
        }
        {
          name: 'WEBSITE_CONTENTAZUREFILECONNECTIONSTRING'
          value: 'DefaultEndpointsProtocol=https;AccountName=${storageAccount.name};AccountKey=${storageAccount.listKeys().keys[0].value}'
        }
        {
          name: 'WEBSITE_CONTENTSHARE'
          value: toLower('name')
        }
        {
          name: 'FUNCTIONS_EXTENSION_VERSION'
          value: '~4'
        }
        {
          name: 'APPINSIGHTS_INSTRUMENTATIONKEY'
          value: appInsightsComponents.properties.InstrumentationKey
        }
        {
          name: 'FUNCTIONS_WORKER_RUNTIME'
          value: 'java'
        }
      ]
    }
  }
}

param hostname string = 'saf.malliina.site'

resource siteCustomDomain 'Microsoft.Web/sites/hostNameBindings@2021-02-01' = {
  name: '${azureFunction.name}/${hostname}'
  properties: {
    hostNameType: 'Verified'
    sslState: 'Disabled'
    customHostNameDnsRecordType: 'CName'
    siteName: azureFunction.name
  }
}

resource certificate 'Microsoft.Web/certificates@2021-02-01' = {
  name: hostname
  location: location
  dependsOn: [
    siteCustomDomain
  ]
  properties: {
    canonicalName: hostname
    serverFarmId: appServicePlan.id
  }
}

module siteEnableSni 'sni-enable.bicep' = {
  name: '${deployment().name}-${azureFunction.name}-sni-enable'
  params: {
    certificateThumbprint: certificate.properties.thumbprint
    hostname: hostname
    siteName: azureFunction.name
  }
}
