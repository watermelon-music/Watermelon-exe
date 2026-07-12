const fetch = require('https');
fetch.get('https://raw.githubusercontent.com/TeamPiped/Piped/master/instances.json', (res) => {
    let body = '';
    res.on('data', chunk => body += chunk);
    res.on('end', () => {
        try {
            const list = JSON.parse(body);
            console.log('Instances:', list.slice(0, 5).map(i => i.api_url));
        } catch(e) {}
    });
});
