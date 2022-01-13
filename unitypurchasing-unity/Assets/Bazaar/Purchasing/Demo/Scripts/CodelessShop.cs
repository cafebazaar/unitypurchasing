using System;
using RTLTMPro;
using UnityEngine;

public class CodelessShop : MonoBehaviour
{
    [SerializeField] private Vehicle vehicle;
    [SerializeField] private RTLTextMeshPro ConsoleText;


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
            default: return;
        }
    }
    public void Log(string message)
    {
        ConsoleText.text = ConsoleText.OriginalText + message + "\n";
    }

}
