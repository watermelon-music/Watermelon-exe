const url = 'https://api.cobalt.tools/api/json';
const data = { url: 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', downloadMode: 'audio', audioFormat: 'best' };
fetch(url, {
    method: 'POST',
    headers: {
        'Accept': 'application/json',
        'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
})
.then(res => res.json())
.then(json => console.log('Cobalt Response:', json))
.catch(err => console.error('Error:', err));
