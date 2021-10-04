# Auto-Updater

<p align="center">
<a href="https://github.com/DeDiamondPro/Auto-Updater/releases" target="_blank">
<img alt="release" src="https://img.shields.io/github/v/release/DeDiamondPro/Auto-Updater?color=00FFFF&style=for-the-badge" />
</a>
<a href="https://github.com/DeDiamondPro/Auto-Updater/releases" target="_blank">
<img alt="downloads" src="https://img.shields.io/github/downloads/DeDiamondPro/Auto-Updater/total?color=00FFFF&style=for-the-badge" />
</a>
<a href="https://github.com/DeDiamondPro/Auto-Updater/blob/master/LICENSE">
    <img alt="license" src="https://img.shields.io/github/license/DeDiamondPro/Auto-Updater?color=00FFFF&style=for-the-badge">
 </a>
  <a href="https://github.com/DeDiamondPro/Auto-Updater/">
    <img alt="lines" src="https://img.shields.io/tokei/lines/github/DeDiamondPro/Auto-Updater?color=00FFFF&style=for-the-badge">
 </a>
    <a href="https://discord.gg/ZBNS8jsAMd" target="_blank">
    <img alt="discord" src="https://img.shields.io/discord/822066990423605249?color=00FFFF&label=discord&style=for-the-badge" />
  </a>
 </p>
 
 ### This mods objective is to automatically update all of your mods before Minecraft starts!
 
 It can be easily configured by typing /autoupdater, it will try to find github links for your mods automatically but might fail at this.
 You can put your own GitHub link in the text box. This link must match the regex `(https://)?(github\.com/)?(?<user>[\w-]{0,39})(/)(?<repo>[\w-]{0,40})(.*)`.
 The updater doesn't update any of your mods by default, this is to avoid that it downgrades any of your mods if the latest release isn't on github.
 It also doesn't update the name of your mod file, why that is I will get into in the techinal detail part.
 You can also configure the updater to use pre-releases of mods if you want it to.
 Screenshot of config menu:
 
 ![Config Menu](https://i.imgur.com/3aTgBAv.png)
 
 ## Technical details
 
 ### Getting latest release
 
 First the updater gets the latest release, to do this it uses the github api `https://api.github.com/repos/user/repo/releases`.
 Once it has done that it will compare the tag to the current tag and if it should update it it does, if it doesn't know the current tag (first use) it will download it even if it already has the latest version.
 
 ### Downloading the update

After it has gotten the link to download it from the api it will download the file in a cache folder (`config/AutoUpdater/cache`).
Once it is there it checks if the Minecraft version matches, if it does it proceeds, if not it downloads a different asset/release.
Then it tries to delete the current version and move the new version from cache to your mods folder.
If it cannot delete the current version it will retry at shutdown.
When it does it at shutdown things are different, instead of just copying it we overwrite the current file since it could still be in use (code for this from Wynntils and Skytils). This is also how it can update itself.

### Why the name of the mod can't be changed

When we start to update mods forge has already made a list of mods that have a loading plugin, so if we change the name of the mod the loading plugin wont be applied for a mod and this can lead to crashes.
