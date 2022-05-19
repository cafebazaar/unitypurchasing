#if UNITY_EDITOR
using System.IO;
using UnityEditor.Build;
using UnityEditor.Build.Reporting;
using UnityEngine;
using UnityEngine.Purchasing;

class ManifestWriter : IPreprocessBuildWithReport
{
    public int callbackOrder => 0;

    public void OnPreprocessBuild(BuildReport report)
    {
        // Load AppStore
        var appStore = StoreData.LoadStore();

        // Load Manifest Template
        var storeData = StoreData.data.ContainsKey(appStore) ? StoreData.data[appStore] : new StoreData();
        var thisScriptDir = Path.GetDirectoryName(new System.Diagnostics.StackTrace(true).GetFrame(0).GetFileName());
        var manifestTempPath = Path.Combine(thisScriptDir, "AndroidManifestTemp.xml");
        var manifest = File.ReadAllText(manifestTempPath);

        // Replace Manifest 
        manifest = manifest.Replace("___MarketQueries___", storeData.manifestQueries);
        manifest = manifest.Replace("___MarketPermissions___", storeData.manifestPermission);
        manifest = manifest.Replace("___MarketActivities___", storeData.manifestActivity);
        manifest = manifest.Replace("___MarketReceivers___", storeData.manifestReceiver);

        // Generate Plugins/Android/AndroidManifest.xml
        var pluginsDir = new DirectoryInfo(Path.Combine(Directory.GetCurrentDirectory(), "Assets", "Plugins"));
        if (!pluginsDir.Exists)
        {
            pluginsDir.Create();
        }

        var androidDir = new DirectoryInfo(Path.Combine(pluginsDir.FullName, "Android"));
        if (!androidDir.Exists)
        {
            androidDir.Create();
        }

        File.WriteAllText(Path.Combine(androidDir.FullName, "AndroidManifest.xml"), manifest);

        // Provide appropriate aar files
        var storesPath = Path.Combine(Directory.GetCurrentDirectory(), "Assets", "Bazaar", "Purchasing", "Plugins", "UnityPurchasing", "Android", "Stores");
        var storesDir = new DirectoryInfo(storesPath);
        if (!storesDir.Exists)
        {
            storesDir.Create();
        }

        foreach (var store in storesDir.GetFiles())
        {
            store.Delete();
        }

        if (storeData.aarFiles != null)
        {
            foreach (var aar in storeData.aarFiles)
            {
                var storeName = aar + ".aar";
                var sourceFile = Path.Combine(thisScriptDir, storeName);
                var destFile = Path.Combine(storesDir.FullName, storeName);
                File.Copy(sourceFile, destFile);
                Debug.Log(sourceFile);
                Debug.Log(destFile);
            }
        }
    }
}
#endif