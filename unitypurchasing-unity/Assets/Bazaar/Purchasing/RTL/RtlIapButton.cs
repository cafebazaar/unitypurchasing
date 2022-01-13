using RTLTMPro;
using UnityEngine.UI;

namespace UnityEngine.Purchasing
{
    /// <summary>
    /// A GUI component for exposing the current price and allow purchasing of In-App Purchases. Exposes configurable
    /// elements through the Inspector.
    /// </summary>
    /// <seealso cref="CodelessIAPStoreListener"/>
    [RequireComponent(typeof(Button))]
    [AddComponentMenu("In-App Purchasing/IAP Button (RTL)")]
    [HelpURL("https://docs.unity3d.com/Manual/UnityIAP.html")]
    public class RtlIapButton : IAPButton
    {
        /// <summary>
        /// Displays the localized title from the app store.
        /// </summary>
        [Tooltip("[Optional] Displays the localized title from the app store.")]
        public RTLTextMeshPro titleRTLText;

        /// <summary>
        /// Displays the localized description from the app store.
        /// </summary>
        [Tooltip("[Optional] Displays the localized description from the app store.")]
        public RTLTextMeshPro descriptionRTLText;

        /// <summary>
        /// Displays the localized price from the app store.
        /// </summary>
        [Tooltip("[Optional] Displays the localized price from the app store.")]
        public RTLTextMeshPro priceRTLText;

        override public void UpdateText()
        {
            var product = CodelessIAPStoreListener.Instance.GetProduct(productId);
            if (product != null)
            {
                if (titleRTLText != null)
                {
                    titleRTLText.text = product.metadata.localizedTitle;
                }

                if (descriptionRTLText != null)
                {
                    descriptionRTLText.text = product.metadata.localizedDescription;
                }

                if (priceRTLText != null)
                {
                    priceRTLText.text = product.metadata.localizedPriceString;
                }
            }
        }
    }
}
