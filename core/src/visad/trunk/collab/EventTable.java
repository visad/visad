package visad.collab;

public interface EventTable
{
  MonitorEvent add(MonitorEvent evt);
  MonitorEvent removeEvent(MonitorEvent evt);
}
