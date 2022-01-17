using System;
using RTLTMPro;
using UnityEngine;

public class CodelessShop : MonoBehaviour
{
    [SerializeField] protected Vehicle vehicle;
    [SerializeField] protected RTLTextMeshPro consoleText;

    public void UpdateStats(String productId)
    {
        switch (productId)
        {
            case "infinite_gas_monthly":
                vehicle.SetGas(5);
                break;
            case "gas":
                vehicle.Increase();
                break;
            case "premium":
                vehicle.SetSkin(1);
                break;
        }
    }

    protected void Log(string message)
    {
        consoleText.text = consoleText.OriginalText + message + "\n";
    }

}
