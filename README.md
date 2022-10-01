# CurrencyConverter
![currency converter](https://github.com/Xia-Sam/CurrencyConverter/blob/master/app/src/main/res/drawable/currency_converter.jpg)

This an Android App that helps calculate the exchange rate from one currency to another.

### Code architecture:

### Features:
  1. To make the app usable when there's no internet connection, all supported currencies are stored locally so users can still choose the base and target code in the spinners. Converting history is also stored so when user choose a base and target code pair existing in the database, the result is directly shown. However, if this pair has never been converted before when network is connected, no result is given. Here, reason to store converting history instead of all base-target pairs is because it really takes a long time to request for converting rates of all currencies. Additionally, not every currency pair will be needed so it will be waste of space. Finally, a lot of API calls need to be made and this is also burden for server.    
  2. Once network is disconnected while user is using the app, network monitor will pop up a dialog to remind user. User can also directly go to the network setting to make further actions.   
  3. For better user experience, user's last converting request will be remembered. Even if the app is closed, next time user opens the app, all the information including currencies, converting result and last-updated-time are restored.   

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
  
Room is used to store data locally. The app needs local database because we want users can still use the app when network is not available, though the data stored may not be up-to-date. Reasons why I choose database instead of sharedPreferences:
  1. data types that can be stored in sharedPreferences are very limited
  2. each piece of data needs an associated key
  3. in the future, we may have more data to store so Room database is more powerful and will meet our future requirements   
However, sharedPreferences is used to store the app state.    

### Bugs encountered and how I solved them:
  1. socket failed: EPERM (Operation not permitted)   
After entering the app, it gives me a toast about this error. Through googling, I found this can be easily solved by uninstalling the app and running it again. The reason is that I forgot to add internet permission in my first installation. Then the OS thought the app doesn't need internet permission and would not check any permission update to the app. Therefore, to tell the Android OS to check updated permission, what we can do is to uninstall the app first before installing again.
  2. After clicking the convert button, the result is always the same as the input amount   
After getting this bug, I checked whether my api is working fine or not. Then through debugging, I found the base code and target code have never changed. Since api is working fine, some part of my logic should be wrong. After checking my code, I found the problem inside the function onItemSelected. This function contains the logic to find the right spinner to update the base code and target code. To find which spinner has been clicked, the first parameter of type AdapterView should be checked but not the second parameter of type View.    
  3. android.view.ViewRootImpl$CalledFromWrongThreadException: Only the original thread that created a view hierarchy can touch its views    
This bug happens when I try to show a toast in the network callback. It reminds me that in Android, only the main thread can update views. When this is done from a worker thread, an exception is thrown. So I launched a coroutine with 'Dispatchers.Main' passed to show the toast hence the problem is solved.
  
### Further improvements and features in the future:
  1. user account: Each user can use their email address and password to login to the app.
  2. in-app advertisements: We can show in-app advertisements to users to make profits.
  3. nation flags for currencies: A national flag can be put next to the currency code, which will speed up the process of choosing the right currency for users. Since images are sometimes more conspicuous than text. 
  4. payment: Users can make payments to get some priviliges, e.g. subscription for no in-app advertisements, unlimited amount of request per month etc.

### Versions until now:
  release v3: add Room database to store all supported currencies and converting history    
  release v2: add error handling for no internet and invalid API key, last updated time, network monitor    
  release v1
