package ru.somarov.deletion_service.domain.entity.jsonb;

import ru.somarov.deletion_service.domain.entity.DeletionProcess;
import ru.somarov.deletion_service.domain.entity.ProcessStageAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * This class is a dto for storing data in jsonb format
 *
 * @author alexandr.omarov
 * @version 1.0.0
 * @since 1.0.0
 * @see ru.somarov.deletion_service.domain.entity.ProcessCompletionLog
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Completion {
    private DeletionProcess process;
    private List<ProcessStageAction> failedActions;
}
