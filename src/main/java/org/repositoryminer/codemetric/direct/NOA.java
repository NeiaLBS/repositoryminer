package org.repositoryminer.codemetric.direct;

import java.util.List;

import org.bson.Document;
import org.repositoryminer.ast.AST;
import org.repositoryminer.ast.AbstractClassDeclaration;
import org.repositoryminer.ast.ClassArchetype;
import org.repositoryminer.ast.ClassDeclaration;
import org.repositoryminer.ast.FieldDeclaration;
import org.repositoryminer.codemetric.CodeMetricId;

/**
 * <h1>Number of attributes</h1>
 * <p>
 * NOA is defined as the number of attributes in a class.
 */
public class NOA implements IDirectCodeMetric {

	private static final String[] UNACCEPTED_TYPES = new String[] { "enum", "class" };

	@Override
	public CodeMetricId getId() {
		return CodeMetricId.NOA;
	}
	
	@Override
	public Document calculate(AbstractClassDeclaration type, AST ast) {
		if (ClassArchetype.CLASS_OR_INTERFACE == type.getArchetype()) {
			ClassDeclaration cls = (ClassDeclaration) type;
			new Document("metric", CodeMetricId.NOA.toString()).append("value", calculate(cls.getFields()));
		}
		return null;
	}

	public int calculate(List<FieldDeclaration> fields) {
		int noa = 0;
		for (FieldDeclaration field : fields) {
			if (accept(field.getType())) {
				noa++;
			}
 		}
		return noa;
	}
	
	private boolean accept(String type) {
		boolean accept = true;
		
		for (String unaccepted : UNACCEPTED_TYPES) {
			if (type.equals(unaccepted)) {
				accept = false;
				
				break;
			}
		}
		
		return accept;
	}

}