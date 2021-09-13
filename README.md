## Unity purchasing v 4.0 ( Cafebazaar support )



### Step 1 :
Import latest unity package from [releases](https://github.com/manjav/unitypurchasing-cafebazaar/releases) section.


<img src="https://github.com/manjav/unitypurchasing-cafebazaar/blob/master/images/Unity-package.png?raw=true"/><br/><br/>

### Step 2 :
Add `IAP Button` to the scene and then select. In inspector click on `IAP Catalog...` to appears IAP Catalog panel.

<img src="https://github.com/manjav/unitypurchasing-cafebazaar/blob/master/images/Button-add.png?raw=true"/><img src="https://github.com/manjav/unitypurchasing-cafebazaar/blob/master/images/Button-inspector.png?raw=true"/><img src="https://github.com/manjav/unitypurchasing-cafebazaar/blob/master/images/Catalog.png?raw=true"/><br/><br/>

### Step 3 - Add Product Item :
1. Check automaticlly initialize ...
2. Select ID and type of product.
3. You can override title, description and override ID for every store.

<img src="https://github.com/manjav/unitypurchasing-cafebazaar/blob/master/images/Catalog-fill.png?raw=true"/><br/><br/>

### Step 4 (Button setup) :
1. Select SKU.
2. Assign title, description and price texts.
3. Delegate purchase complete/failure method.

<img src="https://github.com/manjav/unitypurchasing-cafebazaar/blob/master/images/Button-setup.png?raw=true"/><br/><br/>

### Step 5 :
Override packagename in Edit -> Project Settings -> Player -> Other Settings

<img src="https://github.com/manjav/unitypurchasing-cafebazaar/blob/master/images/Override-packagename.png?raw=true"/><br/><br/>

### Step 6 :
Select your store: Open BillingMode file in Resources and change `androidStore` from `Google` to `Cafebazaar` or another stores.
```
{"androidStore":"Cafebazaar"}
```