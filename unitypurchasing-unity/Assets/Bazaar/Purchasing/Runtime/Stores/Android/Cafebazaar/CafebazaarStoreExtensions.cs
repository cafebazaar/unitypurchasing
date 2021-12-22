namespace UnityEngine.Purchasing
{
    /// <summary>
    /// Access Cafebazaar store specific functionality.
    /// </summary>
    public class CafebazaarStoreExtensions
	{
		private AndroidJavaObject android;
		/// <summary>
		/// Build the CafebazaarStoreExtensions with the instance of the Cafebazaar java object
        /// </summary>
		/// <param name="a">Cafebazaar java object</param>
        public CafebazaarStoreExtensions(AndroidJavaObject a) {
			this.android = a;
		}
	}
}
