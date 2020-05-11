package de.embl.cba.plateviewer.view;

import de.embl.cba.plateviewer.table.BatchLibHdf5CellFeatureProvider;
import ij.gui.GenericDialog;

public class CellFeatureDialog
{
	private final BatchLibHdf5CellFeatureProvider cellFeatureProvider;
	private final String[] tableGroups;
	private String tableGroupChoice;
	private String featureChoice;


	public CellFeatureDialog( BatchLibHdf5CellFeatureProvider cellFeatureProvider )
	{
		this.cellFeatureProvider = cellFeatureProvider;
		tableGroups = cellFeatureProvider.getTableGroups().toArray( new String[]{} );
		tableGroupChoice = tableGroups[ 0 ];
	}

	public void showDialog( String siteName, int cellId )
	{
		// fetch table group
		final GenericDialog groupDialog = new GenericDialog( "" );
		groupDialog.addChoice( "Feature group", tableGroups, tableGroupChoice );

		groupDialog.showDialog();
		if ( groupDialog.wasCanceled() ) return;

		tableGroupChoice = groupDialog.getNextChoice();

		// fetch feature
		final String[] features = cellFeatureProvider.getFeatureNames( siteName, tableGroupChoice ).stream().toArray( String[]::new );

		final GenericDialog featureDialog = new GenericDialog( "" );
		featureDialog.addChoice( "Feature", features, featureChoice );

		featureDialog.showDialog();
		if ( featureDialog.wasCanceled() ) return;

		featureChoice = featureDialog.getNextChoice();


		cellFeatureProvider.fetchFeature( cellId, siteName, tableGroupChoice, featureChoice );
	}
}
