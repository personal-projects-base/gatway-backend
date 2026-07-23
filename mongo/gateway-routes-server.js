// Execute no MongoDB do servidor:
// mongosh "mongodb://localhost:27017/logapi" gateway-routes-server.js
// Altere os IPs abaixo para os IPs WireGuard ou privados reais das máquinas.
const routes = db.getCollection('gateway_routes');
const rewrite = [{ name: 'RewritePath', args: {
  _genkey_0: '/api/(?<segment>.*)', _genkey_1: '/${segment}'
}}];

const definitions = [
  ['church-lite-backend', 'church-lite', '/api/church-lite/**', ['http://10.8.0.2:5050']],
  ['smart-report-backend', 'smart-report', '/api/smartreport/**', ['http://10.8.0.2:5070', 'http://10.8.0.3:5071']],
  ['smart-game-bank', 'smart-game-bank', '/api/game-bank/**', ['http://10.8.0.2:8020']]
];

definitions.forEach(([id, service, path, uris]) => routes.updateOne(
  { _id: id },
  { $set: { _id: id, service, uri: `lb://${service}`, order: 0, enabled: true,
      instances: uris.map(uri => ({ uri, enabled: true })),
      predicates: [{ name: 'Path', args: { _genkey_0: path } }],
      filters: rewrite, metadata: {} } },
  { upsert: true }
));

print('Rotas do servidor criadas/atualizadas.');
