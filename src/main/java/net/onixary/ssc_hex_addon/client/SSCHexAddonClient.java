package net.onixary.ssc_hex_addon.client;

import net.fabricmc.api.ClientModInitializer;
import net.onixary.shapeShifterCurseFabric.mana.ManaRegistriesClient;
import net.onixary.ssc_hex_addon.SSCHexAddon;

public class SSCHexAddonClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        // 四个媒介类型共用一个渲染器，因为玩家同时最多只有一个激活
        HexFoxMediaBar3 sharedBar = new HexFoxMediaBar3();

        ManaRegistriesClient.registerManaTypeRender(SSCHexAddon.HEX_FOX_MEDIA_0, sharedBar);
        ManaRegistriesClient.registerManaTypeRender(SSCHexAddon.HEX_FOX_MEDIA_1, sharedBar);
        ManaRegistriesClient.registerManaTypeRender(SSCHexAddon.HEX_FOX_MEDIA_2, sharedBar);
        ManaRegistriesClient.registerManaTypeRender(SSCHexAddon.HEX_FOX_MEDIA_3, sharedBar);



    }
}