using UnityEditor;
using UnityEngine;
using UnityEngine.Purchasing;

namespace UnityEditor.Purchasing
{
    /// <summary>
    /// IAPRTLButtonMenu class creates options in menus to create the <see cref="IAPButton"/>.
    /// </summary>
    public static class RtlIapButtonMenu
    {
        /// <summary>
        /// Add option to create a IAPButton from the GameObject menu.
        /// </summary>
        [MenuItem("GameObject/In-App Purchasing/RTL IAP Button", false, 10)]
        public static void GameObjectCreateUnityIAPButton()
        {
            CreateUnityIAPButtonInternal();
        }

        /// <summary>
        /// Add option to create a IAPButton from the Window/UnityIAP menu.
        /// </summary>
        [MenuItem("Services/In-App Purchasing/Create IAP RTL Button", false, 100)]
        public static void CreateUnityIAPButton()
        {
            CreateUnityIAPButtonInternal();
        }

        static void CreateUnityIAPButtonInternal()
        {
            GameObject buttonObject = CreateButtonObject();

            if (buttonObject)
            {
                var iapButton = buttonObject.AddComponent<RtlIapButton>();

                if (iapButton != null)
                {
                    UnityEditorInternal.ComponentUtility.MoveComponentUp(iapButton);
                    UnityEditorInternal.ComponentUtility.MoveComponentUp(iapButton);
                    UnityEditorInternal.ComponentUtility.MoveComponentUp(iapButton);
                }
            }
        }

        static GameObject CreateButtonObject()
        {
            ExecuteButtonMenuItem();

            return Selection.activeGameObject;
        }

        static void ExecuteButtonMenuItem()
        {
#if UNITY_2022_1_OR_NEWER || (UNITY_2021_2_OR_NEWER && !(UNITY_2021_2_2 || UNITY_2021_2_1))
            EditorApplication.ExecuteMenuItem("GameObject/UI/Legacy/Button");
#else
            EditorApplication.ExecuteMenuItem("GameObject/UI/Button - RTLTMP");
#endif
        }
    }
}

