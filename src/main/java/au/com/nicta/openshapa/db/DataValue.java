/*
 * DataValue.java
 *
 * Created on December 6, 2006, 12:46 PM
 *
 */

package au.com.nicta.openshapa.db;

/**
 * Class DataValue
 *
 * Instances of subclasses of the abstract class DataValue are used to 
 * store individual pieces of data in the database, or as headers of 
 * predicates.
 *
 * Each DataValue is associated with with a formal argument, and is
 * constrained to the type (if any) of that formal argument.  The DataValue
 * must listen for changes in the formal argument, and accomodate itself
 * to them. 
 *
 * Each type of data value will have fields specific to its type, but 
 * their common fields and methods are defined here.
 *
 *                                              JRM -- 7/21/07  
 *
 * @author FGA
 */
public abstract class DataValue extends DBElement
{
    /*************************************************************************/
    /***************************** Fields: ***********************************/
    /*************************************************************************/
    /*
     * itsFargID:   Long containingthe ID of the formal argument of which this
     *      DataValue is an assignment.  In the rare case in which this 
     *      instance is not associated with any formal argument, this field
     *      must be set to DBIndex.INVALID_ID.
     *
     * itsFargType: fArgType indicating the type of the formal argument 
     *      specified in itsFargID above.  Set this field to UNDEFINED if 
     *      itsFargID is DBIndex.INVALID_ID.
     *
     * subRange:  Boolean flag set to true iff the associated formal argument
     *      is sub-ranged. (i.e. an integer restricted to the range [1, 100].
     *
     * itsCell: Reference to the instance of Cell in which this DataValue
     *      appears, or null if it is not currently associated with a cell.
     *
     * itsCellID:  Long containing the ID of the cell in which this data value
     *      appears, or DBIndex.INVALID_ID if it is not currently associated
     *      with a cell.  Note that this field exists primarily for sanity
     *      checking.
     *
     * itsPredID:  Long containing the ID of the predicate or column predicate
     *      in whose argument list this data value appears (if any).  When the 
     *      DatavValue doesn't appear in any predicate, the field is set to 
     *      DBIndex.INVALID_ID.
     */

    /** ID of associated formal argument */
    long itsFargID = DBIndex.INVALID_ID;
    
    /** type of associated formal argument */
    FormalArgument.fArgType itsFargType = FormalArgument.fArgType.UNDEFINED;
    
    /** whether the associated formal argument is subtyped */
    boolean subRange = false;
    
    /** id of the cell in which the DataValue resides */
    long itsCellID = DBIndex.INVALID_ID;
    
    /** id of the Predicate in which which the DataValue resides -- if any. */
    long itsPredID = DBIndex.INVALID_ID;
    
//    /** Data Value Change Listeners */
//    java.util.Vector<DataValueChangeListener> changeListeners = 
//            new java.util.Vector<DataValueChangeListener>();
  
    
    /*************************************************************************/
    /*************************** Constructors: *******************************/
    /*************************************************************************/
    
    /** 
     * DataValue()
     *
     * Constructor for instances of DataValue.  
     * 
     * Two versions of this constructor.  One that takes a reference to
     * a database as its parameter and just calls the super() constructor, 
     * and one that is used to create a copies of subclasses of DataValue.
     *
     *                                              JRM -- 2/28/07  
     *
     * Changes:
     *
     *    - None.
     *      
     */
 
    public DataValue(Database db)
        throws SystemErrorException
    {
        super(db);
    }
    
    public DataValue(DataValue dv)
        throws SystemErrorException
    {
        super(dv);

        this.itsFargID = dv.itsFargID;
        this.itsFargType = dv.itsFargType;
        this.subRange = dv.subRange;
        this.itsCellID = dv.itsCellID;
        this.itsPredID = dv.itsPredID;
    } /* DataValue::DataValue() */
    
        
    /*************************************************************************/
    /******************* Abstract Method Declarations: ***********************/
    /*************************************************************************/
    
    /**
     * toString()
     *
     * Returns a String representation of the DBValue for display.
     * @return the string value.
     *
     * Changes:
     *
     *     - None.
     */
    @Override
    public abstract String toString();


    /**
     * toDBString()
     *
     * Returns a database String representation of the DBValue for comparison 
     * against the database's expected value.<br>
     * <i>This function is intended for debugging purposses.</i>
     * @return the string value.
     */
    @Override
    public abstract String toDBString();
    
    
    /** 
     * updateForFargChange()
     *
     * Update for a change in the formal argument name, and/or subrange.
     *
     *                                          JRM -- 3/22/08
     *
     * Changes:
     *
     *    - None.
     */
    
    public abstract void updateForFargChange(boolean fargNameChanged,
                                             boolean fargSubRangeChanged,
                                             boolean fargRangeChanged,
                                             FormalArgument oldFA,
                                             FormalArgument newFA)
        throws SystemErrorException;
    
    
    /**
     * updateSubRange()
     *
     * Determine if the formal argument associated with the data value is 
     * subranged, and if it is, updates the data values representation of 
     * the subrange (if ant) accordingly.  In passing, coerce the value of
     * the datavalue into the subrange if necessary.
     *
     * The fa argument is a reference to the current representation of the
     * formal argument associated with the data value.
     *
     *                                          JRM -- 8/16/07
     *
     * Changes:
     *
     *    - None.
     */
    
    protected abstract void updateSubRange(FormalArgument fa)
        throws SystemErrorException;
    
        
    /*************************************************************************/
    /***************************** Accessors: ********************************/
    /*************************************************************************/

    /**
     * getItsFargID()
     *
     * Return the ID associated with the formal argument with which this 
     * DataValue is associated.
     *                                              JRM -- 7/22/07
     *
     * Changes:
     *
     *    - None.
     */
    
    public long getItsFargID()
    {
        return this.itsFargID;
    }
    
    /**
     * getItsFargType()
     *
     * Return the type of the formal argument with which this DataValue is 
     * associated.
     *
     * Note that there is no setItsFargType() method, as the itsFargType
     * field is set in passing by setItsFargID().
     *
     *                                              JRM -- 7/23/07
     *
     * Changes:
     *
     *    - None.
     */
    
    public FormalArgument.fArgType getItsFargType()
    {
        return this.itsFargType;
    }
    
    /**
     * getSubRange()
     *
     * Return the value of the subRange flag.  Observe that there is no 
     * setSubRange() method as this field is set in passing by setItsFargID(),
     * and can only be changed by a listener call.
     *
     *                                              JRM -- 7/23/07
     *
     * Changes:
     *
     *    - None.
     */
    
    public boolean getSubRange()
    {
        return this.subRange;
    }
    
    
    /**
     * setItsCellID()
     *
     * Set the ID associated with the cell with which this 
     * DataValue is associated.  In passing, verify that the target 
     * cell exists.
     * 
     * If the DataValue is not associated with a predicate (i.e. itsPredID 
     * == INVALID_ID), then the MatrixVocabElement of the Column in which
     * cell appears must contain the formal argument whose ID is stored in 
     * itsFargID.
     *
     * If, on the other hand, the data value is associated with a predicate,
     * then the supplied cell ID must match the itsCellID of that predicate.
     * 
     * In either case, verify these invarients.
     *
     * Note: if the DataValue does appear in a predicate, then the 
     * PredicateVocabElement associated with the predicate must contain the 
     * formal argument whose ID is stored in itsFargID.  However, we already
     * checked this when we set itsPredID, so no need to check it again here.
     * 
     *                                              JRM -- 11/14/07
     *
     * Changes:
     *
     *    - None.
     */
    
    public void setItsCellID(long ID)
        throws SystemErrorException
    {
        final String mName = "DataValue::setItsCellID(): ";
        boolean matchFound = false;
        int i;
        long mveID;
        DBElement dbe = null;
        DataCell dc = null;
        MatrixVocabElement mve = null;
        FormalArgument fa = null;
        Predicate pred = null;
        
        if ( this.itsFargID == DBIndex.INVALID_ID )
        {
            throw new SystemErrorException(mName + 
                                           "itsFargID INVALID on entry.");
        }
        
        if ( ID == DBIndex.INVALID_ID )
        {
            throw new SystemErrorException(mName + "ID == INVALID_ID");    
        }
        
        if ( this.itsPredID == DBIndex.INVALID_ID )
        {
            /* The data value must be a top level argument of a data cell.
             * Verify this as follows:
             *
             * 1) looking up the cell indicated by the supplied ID
             *
             * 2) look up the MatrixVocabElement associated with the cell
             *
             * 3) Scan the argument list of the mve and verify that it
             *    contains a formal argument with ID matching this.itsFargID.
             */
            dbe = this.getDB().idx.getElement(ID);

            if ( dbe == null )
            {
                throw new SystemErrorException(mName + "ID has no referent");
            }

            if ( ! ( dbe instanceof DataCell ) )
            {
                throw new SystemErrorException(mName + 
                        "ID doesn't refer to a DataCell");
            }

            /* If we get this far, we know that dbe is a DataCell */
            dc = (DataCell)dbe;

            mveID = dc.getItsMveID();

            if ( mveID == DBIndex.INVALID_ID )
            {
                throw new SystemErrorException(mName + "mveID == INVALID_ID");    
            }

            dbe = this.getDB().idx.getElement(mveID);

            if ( dbe == null )
            {
                throw new SystemErrorException(mName + "mveID has no referent");
            }

            if ( ! ( dbe instanceof MatrixVocabElement ) )
            {
                throw new SystemErrorException(mName + 
                        "mveID doesn't refer to a MatrixVocabElement");
            }

            /* If we get this far, we know that dbe is a MatrixVocabElement */
            mve = (MatrixVocabElement)dbe;

            i = 0;
            matchFound = false;
            while ( ( i < mve.getNumFormalArgs() ) && ( ! matchFound ) )
            {
                if ( mve.getFormalArg(i).getID() == itsFargID )
                {
                    matchFound = true;
                }
                i++;
            }

            if ( ! matchFound )
            {
                // todo: delete the following line eventually
                // int j = 1/0;
                throw new SystemErrorException(mName + 
                        "Target cell's mve does not contain itsFarg");
            }
        }
        else /* this.itsPredID != DBIndex.INVALID_ID */
        {
            /* The data value must be a top level argument of the predicate
             * indicated by this.itsPredID.  Verify that the supplied ID 
             * matches the cell ID of the containining predicate.
             */
            dbe = this.getDB().idx.getElement(this.itsPredID);

            if ( dbe == null )
            {
                throw new SystemErrorException(mName + 
                        "this.itsPredID has no referent");
            }

            if ( ! ( dbe instanceof Predicate ) )
            {
                throw new SystemErrorException(mName + 
                        "this.itsPredID doesn't refer to a Predicate");
            }

            /* If we get this far, we know that dbe is a Predicate */
            pred = (Predicate)dbe;
            
            if ( pred.getCellID() != ID )
            {
                throw new SystemErrorException(mName + 
                                               "ID != pred.getCellID()");
            }
        }
        
        this.itsCellID = ID;
        
        return;
        
    } /* DataValue::SetItsCellID() */
    

    /**
     * setItsFargID()
     *
     * Set the ID associated with the formal argument with which this 
     * DataValue is associated.  In passing, verify that the target 
     * formal argument exists, and set itsFargType to match the type 
     * of the formal argument.
     *                                              JRM -- 7/22/07
     *
     * Changes:
     *
     *    - None.
     */
    
    public void setItsFargID(long ID)
        throws SystemErrorException
    {
        final String mName = "DataValue::SetItsFargID(): ";
        DBElement dbe = null;
        FormalArgument fa = null;
        FormalArgument.fArgType fargType;
        
        if ( ID == DBIndex.INVALID_ID )
        {
            throw new SystemErrorException(mName + "ID == INVALID_ID");    
        }
        
        dbe = this.getDB().idx.getElement(ID);
        
        if ( dbe == null )
        {
            throw new SystemErrorException(mName + "ID has no referent");
        }
        
        if ( ! ( dbe instanceof FormalArgument ) )
        {
            throw new SystemErrorException(mName + 
                    "ID doesn't refer to a formal argument");
        }
        
        /* If we get this far, we know that dbe is a FormalArgument */
        fa = (FormalArgument)dbe;
        this.subRange = false; /* will change later if appropriate */
        fargType = fa.getFargType();
        
        switch ( fargType )
        {
            case COL_PREDICATE:
            case FLOAT:
            case INTEGER:
            case NOMINAL:
            case PREDICATE:
            case TIME_STAMP:
                this.itsFargID = ID;
                this.itsFargType = fargType;
                this.updateSubRange(fa);
                break;
            
            case QUOTE_STRING:
            case TEXT:
            case UNTYPED:
                this.itsFargID = ID;
                this.itsFargType = fargType;
                break;
                
            case UNDEFINED:
                throw new SystemErrorException(mName + 
                                               "formal arg type undefined???");
                /* break statement commented out to keep the compiler happy */
                // break;
                
            default:
                throw new SystemErrorException(mName + 
                                               "Unknown Formal Arg Type");
                /* break statement commented out to keep the compiler happy */
                // break;
        }
        
        return;
        
    } /* DataValue::SetItsFargID() */
    
    
    /**
     * setItsPredID()
     *
     * Set the ID associated with the Predicate with which this 
     * DataValue is associated.  In passing, verify that the target 
     * instance of Predicate exists, and that its associated 
     * PredicateVocabElement contains the formal argument whose ID is 
     * stored in itsFargID.
     * 
     *                                              JRM -- 11/14/07
     *
     * Changes:
     *
     *    - None.
     */
    
    public void setItsPredID(long ID)
        throws SystemErrorException
    {
        final String mName = "DataValue::setItsCellID(): ";
        boolean matchFound = false;
        int i;
        long pveID;
        DBElement dbe = null;
        Predicate pred = null;
        PredicateVocabElement pve = null;
        FormalArgument fa = null;
        
        if ( this.itsFargID == DBIndex.INVALID_ID )
        {
            throw new SystemErrorException(mName + 
                                           "itsFargID INVALID on entry.");
        }
        
        if ( ID == DBIndex.INVALID_ID )
        {
            throw new SystemErrorException(mName + "ID == INVALID_ID");    
        }
        
        dbe = this.getDB().idx.getElement(ID);
        
        if ( dbe == null )
        {
            throw new SystemErrorException(mName + "ID has no referent");
        }
        
        if ( ! ( dbe instanceof Predicate ) )
        {
            throw new SystemErrorException(mName + 
                    "ID doesn't refer to a Predicate");
        }
        
        /* If we get this far, we know that dbe is a DataCell */
        pred = (Predicate)dbe;
        
        pveID = pred.getPveID();
        
        if ( pveID == DBIndex.INVALID_ID )
        {
            throw new SystemErrorException(mName + "pveID == INVALID_ID");    
        }
        
        dbe = this.getDB().idx.getElement(pveID);
        
        if ( dbe == null )
        {
            throw new SystemErrorException(mName + "pveID has no referent");
        }
        
        if ( ! ( dbe instanceof PredicateVocabElement ) )
        {
            throw new SystemErrorException(mName + 
                    "pveID doesn't refer to a PredicateVocabElement");
        }
        
        /* If we get this far, we know that dbe is a PredicateVocabElement */
        pve = (PredicateVocabElement)dbe;

        i = 0;
        matchFound = false;
        while ( ( i < pve.getNumFormalArgs() ) && ( ! matchFound ) )
        {
            if ( pve.getFormalArg(i).getID() == itsFargID )
            {
                matchFound = true;
            }
            i++;
        }
        
        if ( ! matchFound )
        {
            throw new SystemErrorException(mName + 
                    "Target pred's pve does not contain itsFarg");
        }
        
        this.itsPredID = ID;
        
        return;
        
    } /* DataValue::SetItsPredID() */
    
  
    /*************************************************************************/
    /***************************** Methods: **********************************/
    /*************************************************************************/
    
    /** 
     * insertInIndex()
     *
     * This method is called when the DataCell, part of whose value is stored in
     * this instance of DataValue, is first inserted in the database and becomes 
     * the first cannonical version of the DataCell.  It is also called if a 
     * new DataValue (i.e. that is a DataValue whose ID has not been assigned) 
     * appears in a new incarnation of the host DataCell.
     * 
     * The method makes note of the DataCell's ID, and inserts the DataValue
     * in the index.
     *
     * Note that PredDataValue must override this method so that its predicate
     * value can insert itself in the index.
     *
     *                                              JRM -- 2/19/08
     *
     * Changes:
     *
     *    - None.
     */
    
    protected void insertInIndex(long DCID)
        throws SystemErrorException
    {
        final String mName = "DataValue::insertInIndex(): ";
        
        // this call does all the sanity checking we need;
        this.setItsCellID(DCID);
        
        // for now at least, DataValue is a subclass of DBElement.  Thus we
        // must insert the DataValue in the index.
        
        this.getDB().idx.addElement(this);
        
        return;
        
    } /* DataValue::insertInIndex(DCID) */
    
    
    /** 
     * removeFromIndex()
     *
     * This method is called when the DataCell part of whose value is stored in
     * this instance of DataValue is deleted from the DataBase.  It is also
     * called if a DataValue is replaced with a new DataValue (i.e. that is 
     * a DataValue whose ID has not been assigned) in a new incarnation of the
     * host DataCell.
     * 
     * The method verifies that the supplied DataCell ID matches the one it 
     * has on file, and then removes itself from the index.
     *
     * Note that PredDataValue must override this method so that its predicate
     * value can remove itself in the index.
     *
     *                                              JRM -- 2/19/08
     *
     * Changes:
     *
     *    - None.
     */
    
    protected void removeFromIndex(long DCID)
        throws SystemErrorException
    {
        final String mName = "DataValue::removeFromIndex(): ";
        
        if ( DCID != this.itsCellID )
        {
            throw new SystemErrorException(mName + "cell ID mismatch.");
        }
        
        // Remove the DataValue from the index.
        
        this.getDB().idx.removeElement(this.getID());
        
        return;
        
    } /* DataValue::removeFromIndex(DCID) */
    
    
    /**
     * replaceInIndex()
     *
     * Update the index to point to this, the new incarnation of the DataValue.
     * The old incarnation is supplied as a parameter for sanity checking.
     *
     * Note that PredDataValue must override this method so as to pass an update
     * an update index for replacement message on to its Predicate.
     *
     *                                          JRM -- 2/20/08
     *
     * Changes:
     *
     *    - None.
     */
    
    protected void replaceInIndex(DataValue old_dv,
                                  long DCID,
                                  boolean cascadeMveMod,
                                  boolean cascadeMveDel,
                                  long cascadeMveID,
                                  boolean cascadePveMod,
                                  boolean cascadePveDel,
                                  long cascadePveID)
        throws SystemErrorException
    {
        final String mName = "DataValue::replaceInIndex(): ";
        
        if ( ( this.getID() != old_dv.getID() ) ||
             ( this.itsCellID != old_dv.itsCellID ) ||
             ( this.itsFargID != old_dv.itsFargID ) ||
             ( this.itsFargType != old_dv.itsFargType ) )
        {
            throw new SystemErrorException(mName + "mis-match with old_dv?!?");
        }
        
        if ( this.itsCellID != DCID )
        {
            throw new SystemErrorException(mName + "DCID mis-match!?!");
        }
        
        this.getDB().idx.replaceElement(this);
        
        return;
        
    } /* DataValue::replaceInIndex() */

    
//    /*************************************************************************/
//    /****************** Change Listener List Management: *********************/
//    /*************************************************************************/
//
//    /**
//     * addChangeListener()
//     *
//     * Add a change listener to this DataValue
//     *
//     * @param listener the change listener to add
//     *
//     * Changes:
//     *
//     *    - None.
//     */
//  
//    public void addChangeListener(DataValueChangeListener listener)
//    {
//        this.changeListeners.add(listener);
//        
//        return;
//        
//    } /* DataValue::addChangeListener() */
//
//    /**
//     * removeChangeListener()
//     *
//     * Removes a change listener from this DataValue
//     *
//     * @param listener the change listener to remove
//     *
//     * Changes:
//     *
//     *    - None.
//     */
//  
//    public void removeChangeListener(DataValueChangeListener listener)
//    {
//        this.changeListeners.remove(listener);
//        
//        return;
//        
//    } /* DataValue::removeChangeListener() */
//
//    /**
//     * notifyListeners()
//     *
//     * Notifies the listeners that this DataValue has changed
//     *
//     * Changes:
//     *
//     *    - None
//     */
//  
//    protected void notifyListeners()
//    {
//        // Loop through vector calling listeners
//        for (int i=0; i<this.changeListeners.size(); i++) 
//        {
//            ((DataValueChangeListener)this.changeListeners.elementAt(i)).dataValueChanged(this);
//        }
//        
//        return;
//        
//    }  /* DataValue::notifyListeners() */
  
    
    /*************************************************************************/
    /************************ Class Methods: *********************************/
    /*************************************************************************/

    /**
     * Copy()
     *
     * Given a DataValue, return a copy.  Only minimal sanity checking.
     * Even less if blindCopy is true. 
     *
     *                                              JRM -- 3/20/08
     *
     * Changes:
     *
     * @Deprecated Should use clone in place of this.
     *
     *    - None.
     */
    @Deprecated
    public static DataValue Copy(DataValue dv,
                                 boolean blindCopy)
        throws SystemErrorException
    {
        final String mName = "DataValue::CopyDataValue(): ";
        DataValue copy = null;
        
        if ( dv == null )
        {
            throw new SystemErrorException(mName + "dv null on entry.");
        }
        
        if ( dv instanceof ColPredDataValue )
        {
            copy = new ColPredDataValue((ColPredDataValue)dv);
        }
        else if ( dv instanceof FloatDataValue )
        {
            copy = new FloatDataValue((FloatDataValue)dv);
        }
        else if ( dv instanceof IntDataValue )
        {
            copy = new IntDataValue((IntDataValue)dv);
        }
        else if ( dv instanceof NominalDataValue )
        {
            copy = new NominalDataValue((NominalDataValue)dv);
        }
        else if ( dv instanceof PredDataValue )
        {
            copy = new PredDataValue((PredDataValue)dv, blindCopy);
        }
        else if ( dv instanceof QuoteStringDataValue )
        {
            copy = new QuoteStringDataValue((QuoteStringDataValue)dv);
        }
        else if ( dv instanceof TextStringDataValue )
        {
            copy = new TextStringDataValue((TextStringDataValue)dv);
        }
        else if ( dv instanceof TimeStampDataValue )
        {
            copy = new TimeStampDataValue((TimeStampDataValue)dv);
        }
        else if ( dv instanceof UndefinedDataValue )
        {
            copy = new UndefinedDataValue((UndefinedDataValue)dv);
        }
        else
        {
            throw new SystemErrorException(mName + 
                                           "Unknown data value sub-type");
        }
        
        return copy;
        
    } /* "DataValue::Copy() */

    /** Seed value for generating hash codes. */
    private final static int SEED1 = 3;
    /** Seed value for generating hash codes. */
    private final static int SEED2 = 7;
    /** Seed value for generating hash codes. */
    private final static int SEED3 = 11;
    /** Seed value for generating hash codes. */
    private final static int SEED4 = 13;
    /** Seed value for generating hash codes. */
    private final static int SEED5 = 17;

    /**
     * @return A hash code value for the object.
     */
    @Override
    public int hashCode() {
        // Assuming id is unique or nearly so, return an int based off it.
        // >>> is unsigned right shift
        int hash = super.hashCode();
        hash += (int)(itsCellID ^ (itsCellID >>> 32)) * SEED1;
        hash += (int)(itsFargID ^ (itsFargID >>> 32)) * SEED2;
        hash += itsFargType.hashCode() * SEED3;
        hash += (int)(itsPredID ^ (itsPredID >>> 32)) * SEED4;
        hash += new Boolean(subRange).hashCode() * SEED5;

        return hash;
    }

    /**
     * Compares this DBElement against another object.
     * Assumption: DBElements are not equal just because their id fields match.
     * This function will test that db, id and lastModUID all match.
     * If id can be proved to be enough for testing equality we should
     * implement a simpler, faster version.
     *
     * @param obj The object to compare this against.
     * @return true if the Object obj is logically equal to this.
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }

        if ((obj == null) || (obj.getClass() != this.getClass())) {
            return false;
        }

        // Must be this class to be here
        DataValue dv = (DataValue) obj;
        return super.equals(obj)
            && (itsCellID == dv.itsCellID)
            && (itsFargID == dv.itsFargID)
            && (itsPredID == dv.itsPredID)
            && (subRange == dv.subRange)
            && (itsFargType == null ? dv.itsFargType == null
                                    : itsFargType.equals(dv.itsFargType));
    }

} //End of DataValue class definition
