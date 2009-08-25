/*
 * ExternalColumnListListener.java
 *
 * Created on February 11, 2008, 10:18 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.openshapa.db;

/**
 * Interface ExternalColumnListListener
 *
 * Objects external to the database that wish to be informed of insertions to
 * and deletions from the column list may implement this interface and then
 * register with the column list of the database of interest.
 *
 * The methods specified in this interface will be called when appropriate.
 *
 *                                                  -- 2/11/08
 */
public interface ExternalColumnListListener
{
    // colDeletion()
    /**
     * Called when a Column is deleted from the column list.
     *
     * The db parameter is mainly for sanity checking, as it is unlikely that
     * any listener will be interested in the column list of more than one
     * Database.
     *
     * The colID parameter contains the ID assigned the column that is
     * being deleted.
     *
     * The old_cov and new_cov parameters contain copies of the column order
     * vector before and after the insertion.
     *
     *                                          -- 2/11/08
     *
     * Changes:
     *
     *    - Added old_cov and new_cov parameters.   -- 7/31/09
     */

    void colDeletion(final Database db,
                      final long colID,
                      final java.util.Vector<Long> old_cov,
                      final java.util.Vector<Long> new_cov);


    // colInsertion()
    /**
     * Called when a Column is inserted in the column list.
     *
     * The db parameter is mainly for sanity checking, as it is unlikely that
     * any listener will be interested in the column list of more than one
     * Database.
     *
     * The colID parameter contains the ID assigned the column that is
     * being inserted.
     * 
     * The old_cov and new_cov parameters contain copies of the column order
     * vector before and after the insertion.
     *
     *                                          -- 2/6/08
     *
     * Changes:
     *
     *    - Added old_cov and new_cov parameters.   -- 7/31/09
     */

    void colInsertion(final Database db,
                      final long colID,
                      final java.util.Vector<Long> old_cov,
                      final java.util.Vector<Long> new_cov);



    // colOrderVectorEdited()
    /**
     * Called when the column order vector is changed for reasons other than
     * column insertion or deletion.
     *
     * The db parameter is mainly for sanity checking, as it is unlikely that
     * any listener will be interested in the column list of more than one
     * Database.
     *
     * The old_cov parameter contains a copy of the column order vector before
     * the change.
     *
     * The new_cov parameter contains a copy of the new column order vector.
     *
     *                                          -- 2/6/08
     *
     * Changes:
     *
     *    - None.
     */

    void colOrderVectorEdited(final Database db,
                              final java.util.Vector<Long> old_cov,
                              final java.util.Vector<Long> new_cov);

}
