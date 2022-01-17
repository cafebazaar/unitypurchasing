using System;
using System.Collections.Generic;
using RTLTMPro;
using UnityEngine;
using UnityEngine.Purchasing;

public class ThroughCodeShop : CodelessShop, IStoreListener
{
    private IStoreController m_StoreController;
    private Dictionary<string, ShopItem> shopItems;
    [SerializeField] private List<Product> products;
    [SerializeField] private ShopItem shopItemTemplate;
    void Start()
    {
        shopItems = new Dictionary<string, ShopItem>();
        InitializePurchasing();
    }

    void InitializePurchasing()
    {
        var builder = ConfigurationBuilder.Instance(StandardPurchasingModule.Instance());

        //Your products IDs. They should match the ids of your products in your store.
        //Add products that will be purchasable and indicate its type.
        builder.AddProduct("gas", ProductType.Consumable);
        builder.AddProduct("premium", ProductType.NonConsumable);
        builder.AddProduct("infinite_gas_monthly", ProductType.Subscription);

        UnityPurchasing.Initialize(this, builder);
    }

    public void OnInitialized(IStoreController controller, IExtensionProvider extensions)
    {
        Log("In-App Purchasing successfully initialized");
        m_StoreController = controller;
        foreach (Transform child in transform)
        {
            GameObject.Destroy(child.gameObject);
        }
        foreach (var p in controller.products.all)
        {
            Log(p.definition.id);
            if (shopItems.ContainsKey(p.definition.id))
            {
                shopItems[p.definition.id].Init(p, m_StoreController.InitiatePurchase);
            }
            else
            {
                shopItems.Add(p.definition.id, Instantiate<ShopItem>(shopItemTemplate, transform).Init(p, m_StoreController.InitiatePurchase));
            }
        }
    }

    public void OnInitializeFailed(InitializationFailureReason error)
    {
        Log($"In-App Purchasing initialize failed: {error}");
    }

    public PurchaseProcessingResult ProcessPurchase(PurchaseEventArgs args)
    {
        //Retrieve the purchased product
        var product = args.purchasedProduct;
        UpdateStats(product.definition.id);

        Log($"Purchase Complete - Product: {product.definition.id}");

        //We return Complete, informing IAP that the processing on our side is done and the transaction can be closed.
        return PurchaseProcessingResult.Complete;
    }

    public void OnPurchaseFailed(Product product, PurchaseFailureReason failureReason)
    {
        Log($"Purchase failed - Product: '{product.definition.id}', PurchaseFailureReason: {failureReason}");
    }
}