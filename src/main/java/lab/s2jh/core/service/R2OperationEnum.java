package lab.s2jh.core.service;

public enum R2OperationEnum {
    

    add ( " Add Association " ) ,

    delete ( " Delete association " ) ,

    update ( " update the association " ) ;

    private String label;

    private R2OperationEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
