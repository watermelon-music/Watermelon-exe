const url = 'https://www.youtube.com/youtubei/v1/player';
const data = {
    context: {
        client: {
            hl: 'en',
            clientName: 'WEB',
            clientVersion: '2.20210721.00.00',
            clientFormFactor: 'UNKNOWN_FORM_FACTOR',
            clientScreen: 'WATCH'
        }
    },
    videoId: 'dQw4w9WgXcQ',
    playbackContext: {
        contentPlaybackContext: {
            signatureTimestamp: 19000
        }
    }
};
fetch(url, {
    method: 'POST',
    headers: {
        'Content-Type': 'application/json'
    },
    body: JSON.stringify(data)
})
.then(res => res.json())
.then(json => {
    if (json.streamingData && json.streamingData.adaptiveFormats) {
        let m4a = json.streamingData.adaptiveFormats.find(f => f.mimeType.includes('audio/mp4'));
        console.log('M4A URL:', m4a ? 'SUCCESS' : 'NOT FOUND');
    } else if (json.playabilityStatus && json.playabilityStatus.status === 'ERROR') {
        console.log('Error:', json.playabilityStatus.reason);
    } else {
        console.log('No streaming data found');
    }
})
.catch(err => console.error('Error:', err));
