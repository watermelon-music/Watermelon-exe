const https = require('https');
https.get('https://api.github.com/repos/TeamNewPipe/NewPipeExtractor/releases/latest', { headers: { 'User-Agent': 'Mozilla/5.0' } }, (res) => {
    let data = '';
    res.on('data', chunk => data += chunk);
    res.on('end', () => console.log(JSON.parse(data).tag_name));
});
