const url = 'https://api.cobalt.tools/';
const data = { url: 'https://www.youtube.com/watch?v=dQw4w9WgXcQ', audioFormat: 'm4a', downloadMode: 'audio' };
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
