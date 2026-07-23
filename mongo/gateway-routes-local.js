// Execute com: mongosh "mongodb://localhost:27017/logapi" gateway-routes-local.js
const routes = db.getCollection('gateway_routes');
const rewrite = [{ name: 'RewritePath', args: {
  _genkey_0: '/api/(?<segment>.*)', _genkey_1: '/${segment}'
}}];

const definitions = [
  ['church-lite-backend', 'church-lite', 'http://localhost:5050', '/api/church-lite/**'],
  ['smart-report-backend', 'smart-report', 'http://localhost:5070', '/api/smartreport/**'],
  ['smart-game-bank', 'smart-game-bank', 'http://localhost:8020', '/api/game-bank/**']
];

definitions.forEach(([id, service, uri, path]) => routes.updateOne(
  { _id: id },
  { $set: { _id: id, service, uri, order: 0, enabled: true,
      instances: [{ uri, enabled: true }],
      predicates: [{ name: 'Path', args: { _genkey_0: path } }],
      filters: rewrite, metadata: {} } },
  { upsert: true }
));

print('Rotas locais criadas/atualizadas.');
