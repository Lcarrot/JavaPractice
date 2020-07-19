const pg = require('pg');
const config = {
    user: 'postgres',
    database: 'MySite',
    password: '1029384756Qq',
    port: 5432
};
const pool = new pg.Pool(config);
module.exports = function (app) {
    app.get('/users', function (req, response, next) {
        pool.connect(function (err, client, done) {
            if (err) {
                // Передача ошибки в обработчик express
                return next(err)
            }
            client.query('SELECT name, e_mail, description FROM users;', [], function (err, result) {
                done()
                if (err) {
                    // Передача ошибки в обработчик express
                    return next(err);
                }
                response.json(result.rows);
            })
        })
    });
    app.post('/users', function (req, res, next) {
        const user = req.body;
        pool.connect(function (err, client, done) {
            if (err) {
                // Передача ошибки в обработчик express
                return next(err)
            }
            client.query('INSERT INTO users (name, e_mail, description) VALUES ($1, $2, $3);', [user.name, user.email, user.request], function (err, result) {
                done() // Этот коллбек сигнализирует драйверу pg, что соединение может быть закрыто или возвращено в пул соединений
                if (err) {
                    // Передача ошибки в обработчик express
                    return next(err)
                }
                res.sendStatus(200)
            })
        })
    });
    app.get('/me', (request, response) => {
        const result = [{
            "id": 1,
            "name": "Леонид",
            "surname": "Тыщенко",
            "description": "Студент КФУ, учусь в ИТИС (типа программист), не очень люблю верстать сайты, поэтому не судите строго ;-).\n" +
                "(Здесь должно быть много текста о том, какой я хороший, и это действительно так, но не вижу смысла много писать, так\n" +
                "как читать это никто не будет :'-( )"
        }];
        response.send(JSON.stringify(result));
    });
}

