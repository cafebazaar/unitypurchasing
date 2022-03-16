## Unity purchasing v 4.0 ( Cafebazaar support )



### Step 1 - Import package:
Import latest unity package from [releases](https://github.com/manjav/unitypurchasing-cafebazaar/releases) section.


<img src="https://github.com/manjav/unitypurchasing-cafebazaar/blob/master/images/Unity-package.png?raw=true"/><br/><br/>

### Step 2 - Add IAP Buttons:
 
<img src="https://github.com/manjav/unitypurchasing-cafebazaar/blob/master/images/Add-IAPButton.webp?raw=true"/>

1. Add `IAP Button` to the scene and then select.
2. In inspector click on `IAP Catalog...` to appears IAP Catalog panel.
3. Add first product (It needs to define products in [Pishkhan](https://pishkhan.cafebazaar.ir) and use their own ID)
4. Select type of product.
5. Override title, description (optional).
6. Override ID for every store (optional - If your products ID are not the same in different stores).
7. Add all product according to steps 3~6.
8. Enable automaticlly initialize ...
9. Close IAP Catalog panel.
10. Assign product.
11. Assign title, description and price texts.
12. Delegate purchase complete/failure method.
13. Add button for all product according to steps 10~12.

<br/><br/>

### Step 3 - Insert packagename:
Override packagename in Edit -> Project Settings -> Player -> Other Settings

<img src="https://github.com/manjav/unitypurchasing-cafebazaar/blob/master/images/Override-packagename.png?raw=true"/><br/><br/>

### Step 4 - Select store:
Select your store: Open `BillingMode` file in Resources and change `androidStore` from `Google` to `Cafebazaar` or other stores.
```
{"androidStore":"Cafebazaar"}
```
<br/>

***

<br/>
<b>Older Unity versions :</b>

If you use unity 2019 and older versions, find and open `bazaar-billing-x.x.x.aar`, then remove `queries` node in `AndroidManifest.xml`.
<br/><br/>
><b>Attention!</b>
>
> Keep in mind, that builds in these versions, do not support android 11+ devices.
