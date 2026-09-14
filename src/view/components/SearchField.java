package view.components;

/** A {@link StyledTextField} preconfigured with a leading search-glass icon and placeholder. */
public class SearchField extends StyledTextField {

    public SearchField(String placeholder) {
        super(placeholder);
        setLeadingIcon(AppIcon.Name.SEARCH);
    }
}
