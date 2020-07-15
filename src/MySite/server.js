const express = require('express');
const app = express();
app.use(express.static('public'));
app.listen(18068);
console.log("Server started at 18068");