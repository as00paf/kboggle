var username = "";
var userId = "";
var gameData = null

function joinGame() {
    const username = document.getElementById('username').value;
    if (!username) {
        alert('Please enter a username');
        return;
    }

    const data = { name: username };

    fetch('/join-game', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    })
    .then(response => response.json())
    .then(data => {
        console.log('Success:', data);
        // Handle the response data here
        onGameJoined(data)
    })
    .catch((error) => {
        console.error('Error:', error);
    });
}

function onGameJoined(data) {
    console.log("onGameJoined")
    this.gameData = data;
    $('#prompt').hide();
    $("#content").css("display", "flex");
    //$('#content').show();
}