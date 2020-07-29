[![DOI](https://zenodo.org/badge/144602584.svg)](https://zenodo.org/badge/latestdoi/144602584)

# PlateViewer

[BigDataViewer](https://imagej.net/BigDataViewer) based [Fiji](https://fiji.sc/) plugin for visual inspection of (multi-well) high throughput microscopy image data.

Features:
- lazy loading of image data for interactive browsing
- support for multi-resolution image data
- segmentation (label mask) visualisation
- linked table, scatterplot and image browsing
- shared coloring model of table scatterplot and image overlay
- integrated (right-click) github issue reporting for efficient collaborative discussion of imaging or image analysis issues
  - automated reporting of plate, well and site as well as automated screenshot generation
  - see example issues [here](https://github.com/hci-unihd/antibodies-analysis-issues/issues)

Future directions:
- explore whether one could develop standardised [CellProfiler](https://cellprofiler.org/) segmentation images and table outputs that could be directly visualized with this tool.

## Usage examples

The plugin has been heavily used in a [Microscopy-based assay for semi-quantitative detection of SARS-CoV-2 specific antibodies in human sera](https://www.biorxiv.org/content/10.1101/2020.06.15.152587v1). Below screenshots show data from this study.

#### Plate view
![image](https://user-images.githubusercontent.com/2157566/88773118-064ec280-d182-11ea-81ee-9806d8de0483.png)

#### Well view
![image](https://user-images.githubusercontent.com/2157566/88773870-07342400-d183-11ea-802a-670f1f0ad3f3.png)

#### Site view
![image](https://user-images.githubusercontent.com/2157566/88774283-87f32000-d183-11ea-89b9-76e9ce923cb9.png)

#### Cell segmentation view
![image](https://user-images.githubusercontent.com/2157566/88774926-5595f280-d184-11ea-8da0-b71afaa10064.png)


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
    
    
## Usage instructions

There is more information to come here, but please explore left and right clicking into the different visualisations.
 
## Cite

This github repository can be cited (registered at [ZENODO](https://zenodo.org/)):
- Tischer, C., and R. Pepperkok (2019) PlateViewer: Fiji plugin for visual inspection of high-throughput microscopy based image data. https://doi.org/10.5281/zenodo.3522688
