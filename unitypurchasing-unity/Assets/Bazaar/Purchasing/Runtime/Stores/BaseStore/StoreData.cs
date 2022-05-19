using System.IO;
using System.Collections.Generic;

namespace UnityEngine.Purchasing
{
    public class StoreData
    {
        public string storePackageName;
        public string bindURL;
        public string manifestPermission;

        public string manifestQueries;
        public string manifestActivity;
        public string manifestReceiver;
        public string downloadURL = "";
        public List<string> aarFiles;
        public static Dictionary<AppStore, StoreData> data = new Dictionary<AppStore, StoreData>
        {
            {
                AppStore.AmazonAppStore, new StoreData { aarFiles = new List<string>{"AmazonAppStore"} }
            },
            {
                AppStore.Cafebazaar, new StoreData {
            downloadURL =   "https://cafebazaar.ir/app/",
        storePackageName =  "com.farsitel.bazaar",
                bindURL =   "ir.cafebazaar.pardakht.InAppBillingService.BIND",
    manifestPermission =    "\n  <uses-permission android:name=\"com.farsitel.bazaar.permission.PAY_THROUGH_BAZAAR\" />\n",
        manifestQueries =   "\n  <queries> <package android:name=\"com.farsitel.bazaar\" /> </queries>",
        manifestActivity =  "\n    <activity"+
                            "\n      android:name=\"com.android.wrapper.WrapperBillingActivity\""+
                            "\n      android:theme=\"@android:style/Theme.Translucent.NoTitleBar.Fullscreen\""+
                            "\n      android:exported=\"true\"/>",
                            aarFiles = new List<string>{ "bazaar-billing", "bazaar-purchasing" }
                }
            },
            {
                AppStore.GooglePlay, new StoreData { downloadURL = "https://play.google.com/store/apps/details?id=" ,
                aarFiles = new List<string>{ "billing-3.0.3" } }
            }
        };

        public static AppStore LoadStore()
        {
#if UNITY_EDITOR
            string path = Path.Combine(System.IO.Directory.GetCurrentDirectory(), "Assets", "Resources", "BillingMode.json");
            string billingMode = File.ReadAllText(path);
            if (billingMode == null)
            {
                return AppStore.NotSpecified;
            }
            return StoreConfiguration.Deserialize(billingMode).androidStore;
#endif
            var textAsset = (Resources.Load("BillingMode") as TextAsset);
            StoreConfiguration config = null;
            if (null != textAsset)
            {
                config = StoreConfiguration.Deserialize(textAsset.text);
            }
            return config.androidStore;
        }
    }
}