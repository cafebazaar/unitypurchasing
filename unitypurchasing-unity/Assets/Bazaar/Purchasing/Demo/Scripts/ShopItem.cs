using System;
using System.Linq;
using RTLTMPro;
using UnityEngine;
using UnityEngine.Purchasing;
using UnityEngine.UI;

public class ShopItem : MonoBehaviour
{
    [SerializeField] private Icon[] icons;
    [SerializeField] private Image iconImage;
    [SerializeField] private RTLTextMeshPro titleText;
    [SerializeField] private RTLTextMeshPro descriptionText;
    [SerializeField] private RTLTextMeshPro priceText;
    private Button button;
    private Product product;
    private Action<string> onSelect;

    public ShopItem Init(Product product, Action<string> onSelect)
    {
        this.product = product;
        this.onSelect = onSelect;
        button = GetComponent<Button>();
        iconImage.sprite = icons.FirstOrDefault(icon => icon.id == product.definition.id).sprite;
        titleText.text = product.metadata.localizedTitle;
        priceText.text = product.metadata.localizedPriceString;
        descriptionText.text = product.metadata.localizedDescription;
        button.interactable = !product.hasReceipt;
        return this;

    }

    public void OnClick()
    {
        onSelect?.Invoke(product.definition.id);
    }
}

[Serializable]
class Icon
{
    public string id;
    public Sprite sprite;
}