var username = "";
var userId = "";
var gameData = null

var timerId = -1;

const ws = new WebSocket('comms'); // Replace with your server's address

ws.onopen = () => {
    console.log('WebSocket connected');
    // Send initial connection message or join game request
};

ws.onmessage = (event) => {
    let data = JSON.parse(event.data);
    handleMessage(data);
};

ws.onclose = () => {
    console.log('WebSocket closed');
    // Handle connection closure
};

ws.onerror = (error) => {
    console.error('WebSocket error:', error);
    // Handle connection errors
};

function handleMessage(data) {
    console.log('Received message: '+ data.type, data);
    switch (data.type) {
        case "GameJoined":
            onGameJoined(data.gameData);
            break;
        case "Sync":
            updateUI(data.gameData);
            break;
        case "WordGuessed":
            onWordGuessed(data);
            break;
    }
}

function joinGame() {
    const username = $('#username').val();
    if (!username) {
        alert("Veuillez entrer un nom d'utilisateur");
        return;
    }

    this.username = username;

    const data = {
        type: "JoinGame",
        name: username
    };
    const payload = JSON.stringify(data)
    console.log("Sending payload :" + payload)
    ws.send("");
    ws.send(payload);
}

function onGameJoined(data) {
    console.log("onGameJoined", data)
    $('#prompt').hide();
    $("#content").css("display", "flex");

    this.gameData = data;
    let user = data.users.find(user => user.name === this.username);
    this.userId = user.id;
    this.username = user.name;
    console.log("Saved local user : " + this.username + " " + this.userId);

    updateUI(data);
    countdownTimer(gameData.currentTime, $('#timer'));
    $('#word').focus();
    if (this.gameData.currentState == "ENDED") {
        onStateChanged(this.gameData.prevState, this.gameData.currentState);
    }
}

function onWordGuessed(data) {
    if (data.points === null) {
        var label = data.word + ", ";
        $("#answerboard").append($("<li>", {
            text: label,
            class: "wrong-word-label"
        }));
        $('#answerboard').scrollTop($('#answerboard')[0].scrollHeight);
        return;
    }
    var label = data.word + "(" + data.points + "), ";
    var listItem = $("<li>", {
        text: label,
        class: "found-word-label"
    });

    $(listItem).hover(
        function() {
            onCurrentWordChanged(data.word);
        },
        function() {
            onCurrentWordChanged("");
        }
    );

    $("#answerboard").append(listItem);
    $('#answerboard').scrollTop($('#answerboard')[0].scrollHeight);
    $('#wordboard').scrollTop($('#wordboard')[0].scrollHeight);
    updateUI(data.gameData);
}

function countdownTimer(duration, display) {
    clearInterval(timerId);
    let timer = duration,
        minutes, seconds;
    timerId = setInterval(function() {
        minutes = parseInt(timer / 60, 10);
        seconds = parseInt(timer % 60, 10);

        seconds = seconds < 10 ? "0" + seconds : seconds;

        display.val(minutes + ":" + seconds);

        if (--timer < 0) {
            updateTimerColor(timer, display);
            clearInterval(timerId);
        } else {
            updateTimerColor(timer, display);
        }

    }, 1000);
}

function updateTimerColor(timer, display){
    if(timer <= 15) {
        display.css("color", "#A52A2F"); //red
    } else if( timer <= 30) {
        display.css("color", "#D3B655");//yellow
    } else {
        display.css("color", "#999");//default
    }
}

function formatTime(seconds) {
    const minutes = Math.floor(seconds / 60);
    const remainingSeconds = seconds % 60;

    const formattedMinutes = String(minutes).padStart(2, '0');
    const formattedSeconds = String(remainingSeconds).padStart(2, '0');

    return `${formattedMinutes}:${formattedSeconds}`;
}

function updateUI(data) {
    // Update game data
    var prevState = this.gameData ? this.gameData.currentState : null;
    this.gameData = data;

    var maxWordsCount = data.currentWords.length;

    if (data.currentState != prevState) { // On state changed to started
        onStateChanged(prevState, data.currentState);
    }

    // Replace letters
    if (data.board != null && data.board.letters != null) {
        for (let i = 1; i <= 16; i++) {
            $('#grid-item-' + i).html(data.board.letters[i - 1].toUpperCase());
        }
    }

    // Clear grid style
    var grid = Array.from(document.querySelectorAll('.game-grid-item'));
    grid.forEach((letter, index) => {
        if(data.isGameStarted) {
            $("#" + letter.id).addClass("started");
            $("#" + letter.id).removeClass("over");
        } else {
            $("#" + letter.id).addClass("over");
            $("#" + letter.id).removeClass("started");
        }
    });

    // Update ranking
    $("#leaderboard").empty();
    var sortedUsers = data.users.sort((a, b) => b.score - a.score);
    sortedUsers.forEach((user, index) => {
        var score = user.score ? user.score : 0;
        var label = (index+1) + ".\t" + user.name + "(" + score + ")"
        var clazz = "user-label";
        if (user.name === this.username) {
            clazz = "local-user-label";
        }
        $("#leaderboard").append($("<li>", {
            text: label,
            class: clazz
        }));
    });

    var user = data.users.find(user => user.name === this.username);

    // Update input word state
    $("#word").prop('disabled', !data.isGameStarted);
    $("#word").attr('tabindex', data.isGameStarted ? "0" : "-1");
    if (data.isGameStarted) {
        $("#word").focus();
    }

    // Update words found
    var foundWords = user ? user.foundWords : [];
    var foundWordsCount = foundWords ? foundWords.length : 0;
    $("#foundWordsLabel").html("Mots Trouvés (" + foundWordsCount + "/" + maxWordsCount + ")");

    // Update score
    let userScore = user ? user.score : 0;
    let scoreLabel = (userScore ? userScore : 0) + " Points";
    $("#user-score").html(scoreLabel);
    let maxScoreLabel = "(Score maximum : " + data.currentMaxScore + " Points)";
    $("#max-score").html(maxScoreLabel);

    // Update winners
    $("#winners").empty()
    data.winners.forEach((user, index) => {
        $("#winners").append($("<li>", {
            text: user.name + " (" + user.score + ")",
            class: "winner-label"
        }));
    });
}

function onStateChanged(prevState, newState) {
    console.log("onStateChanged", prevState, newState);
    // Timer
    console.log("Restarting timer for : " + gameData.currentTime + " seconds")
    countdownTimer(gameData.currentTime, $('#timer'));

    if (newState === "STARTED") {
        // Board
        onCurrentWordChanged("");

        // Ranking
        $("#ranking-label").html("Classement provisoire");

        // Answers
        var maxWordsCount = this.gameData.currentWords.length;
        $("#answerboard").empty();
        $("#foundWordsLabel").html("Mots Trouvés (0/" + maxWordsCount + ")");

        // Word
        $("#word").focus();
    } else if (newState === "ENDED") {
        // Ranking
        var userCount = this.gameData.users.length;
        var sortedUsers = this.gameData.users.sort((a, b) => b.score - a.score);
        var winner = sortedUsers[0];
        var user = this.gameData.users.find(user => user.name === this.username);
        var userRanking = sortedUsers.indexOf(user) + 1;
        var rankingLabel = "(" + userRanking + "/" + userCount + ")";
        $("#ranking-label").html("Classement final " + rankingLabel);

        // Message
        var message = userRanking === 1 ? "<font color='#A52A2F'><b>Vous avez gagné!!!!</b></font>" : "Partie terminée";
        showGameMessage(message);

        // Reset
        onCurrentWordChanged("");
        $("#word").val("");
        $("#answerboard").empty();

        var userFoundWords = user ? user.foundWords : [];

        // Show Possible words
        // Group words by length
        const groupedWords = this.gameData.currentWords.reduce((acc, word) => {
            const length = word.length;
            acc[length] = acc[length] || [];
            acc[length].push(word);
            return acc;
        }, {});

        // Alpha sort
        // Sort words within each group alphabetically
        Object.values(groupedWords).forEach(wordGroup => {
            wordGroup.sort();
        });

        // Create the HTML structure
        const container = document.getElementById("answerboard");
        for (const length in groupedWords) {
            const heading = document.createElement('h3');
            heading.textContent = `Mots de ${length} lettres:`;
            container.appendChild(heading);
            const wordList = document.createElement('ul');
            wordList.classList.add("answerpanel");
            groupedWords[length].forEach((word, index) => {
                var userFoundIt = userFoundWords ? userFoundWords.includes(word) : false;
                var clazz = userFoundIt ? "found-word-label" : "not-found-word-label";

                const listItem = document.createElement('li');
                listItem.classList.add(clazz);
                listItem.textContent = word;
                if (index != groupedWords[length].length - 1) {
                    listItem.textContent += ", ";
                }
                wordList.appendChild(listItem);
            });
            container.appendChild(wordList);
        }

        // Set hover listeners
        const resultWords = Array.from(document.querySelectorAll('.found-word-label, .not-found-word-label'));
        resultWords.forEach((word, index) => {
            $(word).hover(
                function() {
                    var parsedWord = $(word).html().replace(", ", "");
                    onCurrentWordChanged(parsedWord);
                },
                function() {
                    onCurrentWordChanged("");
                }
            );
        });

        $("#answerboard").scrollTop(0);
        $("#wordboard").scrollTop(0);
    } else if(prevState != "RESTARTING" && newState === "RESTARTING") {
        showGameMessage("Une nouvelle partie va bientôt commencer...");
    }
}

function showGameMessage(message) {
    console.log("showGameMessage()", message);
    $("#message-container").html(message);
     $("#message-container").css({
          transform: 'scale(0)',
          opacity: 0
        }).show(); // Show the div before animating

        $("#message-container").animate({
          transform: 'scale(1)',
          opacity: 1
        }, 500) // Scale up and fade in over 500ms
        .delay(1000) // Wait for 1 second
        .animate({
          transform: 'scale(0)',
          opacity: 0
        }, 500); // Scale down and fade out over 500ms
}

function sendWord() {
    var word = $("#word").val().toLowerCase();
    const data = {
        type: "WordGuess",
        userId: this.userId,
        word: word
    };
    const payload = JSON.stringify(data)
    ws.send("");
    ws.send(payload);
    // Clear grid style
    const grid = Array.from(document.querySelectorAll('.game-grid-item'));
    grid.forEach((letter, index) => {
        $("#" + letter.id).removeClass("game-grid-item-highlighted");
    });
}

function onCurrentWordChanged(word) {
    var letters = this.gameData.board ? this.gameData.board.letters : null;
    if (letters === null) {
        return;
    }
    const grid = Array.from(document.querySelectorAll('.game-grid-item'));
    const grid2D = [
        [letters[0], letters[1], letters[2], letters[3]],
        [letters[4], letters[5], letters[6], letters[7]],
        [letters[8], letters[9], letters[10], letters[11]],
        [letters[12], letters[13], letters[14], letters[15]]
    ];
    const path = findWordPath(grid2D, word.toLowerCase());
    grid.forEach((letter, index) => {
        $("#" + letter.id).removeClass("game-grid-item-highlighted");
        if (path != null && path.includes(index)) {
            $("#" + letter.id).addClass("game-grid-item-highlighted");
        }
    });
}

// Key listeners
$(document).ready(function() {
    $('#username').keypress(function(event) {
        if (event.which === 13) {
            $('#joinButton').click();
        }
    });

    $('#username').focus();

    $('#word').keypress(function(event) {
        if (event.which === 13) {
            $('#validateButton').click();
            $('#word').val("");
        }
    });

    $('#word').on('input', function() {
        //console.log('Input value changed:', $(this).val());
        onCurrentWordChanged($(this).val())
    });
});

// Window listeners
$(window).on('beforeunload', function() {
    return 'Are you sure you want to leave?';
});

$(window).on('unload', function() {
    console.log("leaveGame : " + userId);
    const data = {
        type: "LeaveGame",
        userId: this.userId
    };
    const payload = JSON.stringify(data)
    console.log("Sending payload :" + payload)
    ws.send("");
    ws.send(payload);
    clearInterval(timerId);
});

function findWordPath(grid, word) {
    const rows = grid.length;
    const cols = grid[0].length;

    const directions = [
        [-1, -1],
        [-1, 0],
        [-1, 1],
        [0, -1],
        [0, 1],
        [1, -1],
        [1, 0],
        [1, 1]
    ];

    // Create a visited array to keep track of used cells
    const visited = Array(rows).fill().map(() => Array(cols).fill(false));

    const findPathRecursive = (row, col, index, path) => {
        if (index === word.length) {
            return path; // Word found
        }

        if (row < 0 || row >= rows || col < 0 || col >= cols ||
            visited[row][col] || grid[row][col] !== word[index]) {
            return null; // Out of bounds, already visited, or letter mismatch
        }

        visited[row][col] = true; // Mark cell as visited
        const newPath = [...path, row * cols + col];

        for (const [dr, dc] of directions) {
            const newRow = row + dr;
            const newCol = col + dc;
            const result = findPathRecursive(newRow, newCol, index + 1, newPath);
            if (result) {
                return result; // Found a path
            }
        }

        visited[row][col] = false; // Unmark cell if no path found
        return null; // No path found from this position
    };

    for (let row = 0; row < rows; row++) {
        for (let col = 0; col < cols; col++) {
            if (grid[row][col] === word[0]) {
                const result = findPathRecursive(row, col, 0, []);
                if (result) {
                    return result;
                }
            }
        }
    }

    return null; // Word not found
}

function calculateScore(words) {
    const onePoint = words.filter(word => word.length <= 4).length;
    const twoPoints = words.filter(word => word.length === 5).length * 2;
    const threePoints = words.filter(word => word.length === 6).length * 3;
    const fivePoints = words.filter(word => word.length === 7).length * 5;
    const elevenPoints = words.filter(word => word.length >= 8).length * 11;

    return onePoint + twoPoints + threePoints + fivePoints + elevenPoints;
}