console.log("chat.js");

var isLogin = false;
var serverAddr = "http://localhost:2200";

window.onload = function () {
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
    document.getElementsByClassName("whenLoggedIn");
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
  xhr.onreadystatechange = function () {
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
  xhr.open("GET", serverAddr + "/auth/session", true);
  xhr.withCredentials = true;
  xhr.send();
}

globalThis.createUser = function () {
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
  xhr.onreadystatechange = function () {
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
  xhr.open("POST", serverAddr + "/users", true);
  xhr.send(json);
  return false;
}

globalThis.login = function () {
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
  xhr.onreadystatechange = function () {
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
  xhr.open("POST", serverAddr + "/auth/login", true);
  xhr.withCredentials = true;
  xhr.send(json);
  return false;
}

globalThis.logout = function () {
  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function () {
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
  xhr.open("POST", serverAddr + "/auth/logout", true);
  xhr.withCredentials = true;
  xhr.send();
  return false;
}

globalThis.post = function () {
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
  xhr.onreadystatechange = function () {
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
  xhr.open("POST", serverAddr + "/messages", true);
  xhr.withCredentials = true;
  xhr.send(json);
  return false;
}

globalThis.getMessages = function () {
  var xhr = new XMLHttpRequest();
  xhr.onreadystatechange = function () {
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
  xhr.open("GET", serverAddr + "/messages", true);
  xhr.withCredentials = true;
  xhr.send();
  return false;
}

globalThis.updateMessages = function (messages) {
  var messagesBox = document.getElementById("messagesBox");
  messagesBox.innerHTML = "";
  messages.forEach(message => {
    console.log(message);
    var mDiv = document.createElement("div");
    mDiv.innerText = message.userScreenName + " : " + message.body;
    messagesBox.appendChild(mDiv);
  });
}
