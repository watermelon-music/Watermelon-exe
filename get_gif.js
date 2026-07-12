const https = require('https');
const url = 'https://g.tenor.com/v1/search?q=aesthetic+concert+lights+loop&key=LIVDSRZULELA&limit=1';
https.get(url, (res) => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => console.log(JSON.parse(data).results[0].media[0].gif.url));
});
