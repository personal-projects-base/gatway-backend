// Execute com: mongosh "mongodb://localhost:27017/logapi" gateway-routes-example.js
const routes = db.getCollection('gateway_routes');

routes.updateOne(
  { _id: 'smart-report-backend' },
  { $set: {
      _id: 'smart-report-backend',
      service: 'smart-report',
      uri: 'lb://smart-report',
      order: 0,
      enabled: true,
      instances: [
        { uri: 'http://10.8.0.2:5070', enabled: true },
        { uri: 'http://10.8.0.3:5071', enabled: true }
      ],
      predicates: [{ name: 'Path', args: { _genkey_0: '/api/smartreport/**' } }],
      filters: [{ name: 'RewritePath', args: {
        _genkey_0: '/api/(?<segment>.*)', _genkey_1: '/${segment}'
      }}],
      metadata: {}
  }},
  { upsert: true }
);

print('Rota de exemplo criada/atualizada.');
