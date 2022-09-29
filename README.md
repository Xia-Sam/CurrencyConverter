# CurrencyConverter

This an Android App that helps calculate the exchange rate from one currency to another.

### Code architecture:

### Features:

### Api used:
The currency exchange rate is obtained from https://www.exchangerate-api.com/docs/. There is an API request quota of 1500 per month since the API key for requesting is personal.

### Libraries used:
The third-party library Retrofit is used for making HTTP request. Reasons why I choose Retrofit:
  1. Very easy to use
  2. Excellent API documentation
  3. Good support in communities
  4. Faster compared to other libraries
  5. Support both synchronous and asynchronous network request
  6. I'm more familiar with it because I have used it in my last job

### Bugs encountered and how I solved them:
  1. socket failed: EPERM (Operation not permitted)   
After entering the app, it gives me a toast about this error. Through googling, I found this can be easily solved by uninstalling the app and running it again. The reason is that I forgot to add internet permission in my first installation. Then the OS thought the app doesn't need internet permission and would not check any permission update to the app. Therefore, to tell the Android OS to check updated permission, what we can do is to uninstall the app first before installing again.
  3. After clicking the convert button, the result is always the same as the input amount   
After getting this bug, I checked whether my api is working fine or not. Then through debugging, I found the base code and target code have never changed. Since api is working fine, some part of my logic should be wrong. After checking my code, I found the problem inside the function onItemSelected. This function contains the logic to find the right spinner to update the base code and target code. To find which spinner has been clicked, the first parameter of type AdapterView should be checked but not the second parameter of type View.
  
### Further improvements in the future:

### versions until now:
