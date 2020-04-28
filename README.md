[![DOI](https://zenodo.org/badge/144602584.svg)](https://zenodo.org/badge/latestdoi/144602584)

# PlateViewer Fiji plugin

## Install

- Please [install Fiji](fiji.sc)
- Within Fiji, please [enable below Update Site](https://imagej.net/Following_an_update_site): 
    - [X] EMBL-CBA
- Restart Fiji

## Run

- The plugin can be started at [ Plugins > Screening > PlateViewer... ]
    - Most efficiently, just type *Plate* into the Fiji search bar and hit [ Run ]:
![image](https://user-images.githubusercontent.com/2157566/80029189-b6a78d80-84e6-11ea-957d-6fe5f9d07f32.png)
- You will be presented with below user interface: ![image](https://user-images.githubusercontent.com/2157566/80489978-f2b47580-8960-11ea-98d1-3148a9b6849d.png)
    - `Plate images directory`: The folder containing all the images of one plate.
    - `Include sub-folders`: Sometimes the images are stored into some nested sub-folder structure. If this is the case, please check this option (e.g., for the ALMF-EMBL screening naming scheme). If your images are not in sub-folders, please **do not check** this to save time parsing all files and exclude parsing wrong files (e.g., for the batchHdf5 naming scheme).
    - `Only load files matching`: Regular expression to subset your files. Typical choices are `.*.h5` or `.*.tif`
        - Please note that it must be `.*` (and not only a `*`) to match anything.
    - `Load image table`: Check this if you also want to load an associated table, where each row contains information about one image on the plate.
    
    
## Use

There is more information to come here, but please explore left and right clicking into the different visualisations.
 
## Cite

This github repository can be cited (registered at [ZENODO](https://zenodo.org/)):
- Tischer, C., and R. Pepperkok (2019) PlateViewer: Fiji plugin for visual inspection of high-throughput microscopy based image data. https://doi.org/10.5281/zenodo.3522688
