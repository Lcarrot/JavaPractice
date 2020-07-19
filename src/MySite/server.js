//подключаем библиотеку express, body-parser, pg(PostgreSQL);
const express = require('express');
const bodyParser = require('body-parser');
//создаём объект express;
const app = express();
app.use(bodyParser.urlencoded({ extended: true }));
require('./app/routes')(app);
//раздаём папку public
app.use(express.static('public'));
//запускаем на порту 80
app.listen(80);
console.log("Server started at 80");