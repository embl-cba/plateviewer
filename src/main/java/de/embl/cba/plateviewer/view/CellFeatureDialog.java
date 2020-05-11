package de.embl.cba.plateviewer.view;

import de.embl.cba.plateviewer.table.BatchLibHdf5CellFeatureProvider;
import ij.gui.GenericDialog;

import java.util.ArrayList;

public class CellFeatureDialog
{
	private final BatchLibHdf5CellFeatureProvider cellFeatureProvider;
	private final String[] tableGroups;
	private String tableGroupChoice;
	private String featureChoice;
	private String feature;


	public CellFeatureDialog( BatchLibHdf5CellFeatureProvider cellFeatureProvider )
	{
		this.cellFeatureProvider = cellFeatureProvider;
		final ArrayList< String > tableGroups = cellFeatureProvider.getTableGroups();
		this.tableGroups = tableGroups.stream().toArray( String[]::new );
		tableGroupChoice = this.tableGroups[ 0 ];
	}

	public boolean showDialog( String siteName, int cellId )
	{
		// fetch table group
		final GenericDialog groupDialog = new GenericDialog( "" );
		groupDialog.addChoice( "Feature group", tableGroups, tableGroupChoice );

		groupDialog.showDialog();
		if ( groupDialog.wasCanceled() ) return false;

		tableGroupChoice = groupDialog.getNextChoice();

		// fetch feature
		final String[] features = cellFeatureProvider.getFeatureNames( siteName, tableGroupChoice ).stream().toArray( String[]::new );

		final GenericDialog featureDialog = new GenericDialog( "" );
		featureDialog.addChoice( "Feature", features, featureChoice );

		featureDialog.showDialog();
		if ( featureDialog.wasCanceled() ) return false;

		featureChoice = featureDialog.getNextChoice();


		feature = cellFeatureProvider.fetchFeature( cellId, siteName, tableGroupChoice, featureChoice );
		return true;
	}

	public String getFeatureValue()
	{
		return feature;
	}

	public String getTableGroupChoice()
	{
		return tableGroupChoice;
	}

	public String getFeatureChoice()
	{
		return featureChoice;
	}
}
