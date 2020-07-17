const usersRoutes = require('./users_routes');
module.exports = function (app) {
    usersRoutes(app);
}