console.log("chat.js");

var isLogin = false;
var serverAddr = "http://localhost:2200";

window.onload = function() {
  checkCurrentSession();
}

function changeIsLogin(_isLogin) {
  isLogin = _isLogin;
  var whenLoggedInDivs = document.getElementsByClassName("whenLoggedIn");
  console.log(whenLoggedInDivs);
  Array.prototype.forEach.call(whenLoggedInDivs, div => {
    console.log(div);
    div.style.display = isLogin ? "block" : "none";
  });
  var whenLoggedOutInDivs = document.getElementsByClassName("whenLoggedOut");
  Array.prototype.forEach.call(whenLoggedOutInDivs, div => {
    console.log(div);
    div.style.display = isLogin ? "none" : "block";
  });
  if (isLogin) {
    console.log("ログインしている");
    getMessages();
  } else {
    console.log("ログインしていない");
  }
}

function checkCurrentSession() {
  console.log(document.cookie);
  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function() {
    switch (xhr.readyState) {
      case 0:
        break;
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        var _isLogin = xhr.status == 200
        changeIsLogin(_isLogin);
        if (xhr.status == 500) {
          alert("something wrong!");
        }
    }
  }
  xhr.open("GET", serverAddr + "/auth/session", false);
  xhr.withCredentials = true;
  xhr.send();
  xhr.abort();
}

function createUser() {
  var json = null;
  try {
    json = JSON.stringify({
      screenName: document.createUserForm.screenName.value,
      loginName: document.createUserForm.loginName.value,
      password: document.createUserForm.password.value
    });
  } catch {
    console.log("データが揃ってないよん");
    return false;
  }

  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function() {
    switch (xhr.readyState) {
      case 0:
        break;
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        alert("ユーザーが作成できました。ログインしてください。")
        if (xhr.status == 500) {
          alert("something wrong!");
        }
    }
  }
  xhr.open("POST", serverAddr + "/users", false);
  xhr.send(json);
  xhr.abort();

  return false;
}

function login() {
  var json = null;
  try {
    json = JSON.stringify({
      loginName: document.loginForm.loginName.value,
      password: document.loginForm.password.value
    });
  } catch {
    console.log("データが揃ってないよん");
    return false;
  }

  console.log(json);

  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function() {
    switch (xhr.readyState) {
      case 0:
        break;
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        var _isLogin = xhr.status == 200
        changeIsLogin(_isLogin);
        if (xhr.status == 500) {
          alert("something wrong!");
        }
    }
  }
  xhr.open("POST", serverAddr + "/auth/login", false);
  xhr.withCredentials = true;
  xhr.send(json);
  xhr.abort();

  return false;
}

function logout() {
  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function() {
    switch (xhr.readyState) {
      case 0:
        break;
      case 1:
        break;
      case 2:
        break;
      case 3:
        break;
      case 4:
        changeIsLogin(false);
        if (xhr.status == 500) {
          alert("something wrong!");
        }
    }
  }
  xhr.open("POST", serverAddr + "/auth/logout", false);
  xhr.withCredentials = true;
  xhr.send();
  xhr.abort();

  return false;
}

function post() {
  var json = null;
  try {
    json = JSON.stringify({
      body: document.postForm.body.value,
    });
  } catch {
    console.log("データが揃ってないよん");
    return false;
  }

  console.log(json);

  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function() {
    if (xhr.readyState == 4) {
      if (xhr.status == 200) {
        getMessages();
        document.postForm.reset();
      }
      if (xhr.status == 500) {
        alert("something wrong!");
      }
    }
  }
  xhr.open("POST", serverAddr + "/messages", false);
  xhr.withCredentials = true;
  xhr.send(json);
  xhr.abort();

  return false;
}

function getMessages() {
  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function() {
    if (xhr.readyState == 4) {
      if (xhr.status == 200) {
        var messages = JSON.parse(xhr.response);
        console.log(messages);
        updateMessages(messages);
      }
      if (xhr.status == 500) {
        alert("something wrong!");
      }
    }
  }
  xhr.open("GET", serverAddr + "/messages", false);
  xhr.withCredentials = true;
  xhr.send();
  xhr.abort();

  return false;
}

function updateMessages(messages) {
  var messagesBox = document.getElementById("messagesBox");
  messagesBox.innerHTML = "";
  messages.forEach(message => {
    console.log(message);
    var mDiv = document.createElement("div");
    mDiv.innerText = message.userScreenName + " : " + message.body;
    messagesBox.appendChild(mDiv);
  });
}
