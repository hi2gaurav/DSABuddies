import React, { useEffect, useState } from 'react';
import { motion } from 'framer-motion';
import {
  Layers, Save, CheckCircle2, ChevronRight,
  ArrowLeft, Sparkles
} from 'lucide-react';
import { api } from '../lib/api';
import { DesignTemplate, UserDesign } from '../types';
import Card from '../components/ui/Card';
import Badge from '../components/ui/Badge';
import LoadingSpinner from '../components/ui/LoadingSpinner';
import { useToast } from '../components/ui/Toast';

export const DesignCanvasPage: React.FC = () => {
  const [templates, setTemplates] = useState<DesignTemplate[]>([]);
  const [myDesigns, setMyDesigns] = useState<UserDesign[]>([]);
  const [selectedTemplate, setSelectedTemplate] = useState<DesignTemplate | null>(null);
  const [currentDesign, setCurrentDesign] = useState<UserDesign | null>(null);
  const [activeCategory, setActiveCategory] = useState<'ALL' | 'HLD' | 'LLD' | 'MY_DESIGNS'>('ALL');
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [showSolution, setShowSolution] = useState(false);

  // Editor states
  const [draftTitle, setDraftTitle] = useState('');
  const [draftContent, setDraftContent] = useState('');
  const [draftDiagram, setDraftDiagram] = useState('');

  const { show } = useToast();

  useEffect(() => {
    fetchData();
  }, []);

  const fetchData = async () => {
    setLoading(true);
    try {
      const [tpls, designs] = await Promise.all([
        api.getDesignTemplates(),
        api.getMyDesigns(),
      ]);
      setTemplates(tpls);
      setMyDesigns(designs);
    } catch (_) {
      show('Failed to load system design templates', 'error');
    } finally {
      setLoading(false);
    }
  };

  const handleSelectTemplate = (t: DesignTemplate) => {
    setSelectedTemplate(t);
    // Check if user already has a saved design for this template
    const existing = myDesigns.find((d) => d.templateId === t.id);
    if (existing) {
      setCurrentDesign(existing);
      setDraftTitle(existing.title);
      setDraftContent(existing.content || '');
      setDraftDiagram(existing.diagramData || '');
    } else {
      setCurrentDesign(null);
      setDraftTitle(t.title);
      setDraftContent(`## Problem Understanding\n- Functional Requirements:\n- Non-Functional Requirements:\n\n## High Level Architecture\n- Core components:\n- Data flow:\n\n## Database Schema\n- Tables & Primary Keys:\n- Partitioning & Indexing:\n\n## Deep Dives & Bottlenecks\n- Caching Strategy:\n- Failover & Availability:`);
      setDraftDiagram(t.diagramData || '');
    }
    setShowSolution(false);
  };

  const handleSelectUserDesign = (d: UserDesign) => {
    setCurrentDesign(d);
    setDraftTitle(d.title);
    setDraftContent(d.content || '');
    setDraftDiagram(d.diagramData || '');
    if (d.templateId) {
      const matchedTpl = templates.find((t) => t.id === d.templateId);
      if (matchedTpl) setSelectedTemplate(matchedTpl);
    }
  };

  const handleSaveDesign = async () => {
    setSaving(true);
    try {
      if (currentDesign && currentDesign.id) {
        const updated = await api.updateDesign(currentDesign.id, {
          title: draftTitle,
          content: draftContent,
          diagramData: draftDiagram,
        });
        setCurrentDesign(updated);
        show('Architecture design updated! ✓', 'success');
      } else {
        const created = await api.saveDesign({
          templateId: selectedTemplate ? selectedTemplate.id : undefined,
          title: draftTitle,
          content: draftContent,
          diagramData: draftDiagram,
        });
        setCurrentDesign(created);
        show('New architecture design saved! ✓', 'success');
      }
      const refreshed = await api.getMyDesigns();
      setMyDesigns(refreshed);
    } catch (err) {
      show('Failed to save design draft', 'error');
    } finally {
      setSaving(false);
    }
  };

  const filteredTemplates = templates.filter((t) => {
    if (activeCategory === 'ALL') return true;
    return t.category === activeCategory;
  });

  return (
    <div className="space-y-6 max-w-6xl mx-auto">
      {/* Header */}
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-3xl font-black text-gray-900 dark:text-white flex items-center gap-2.5">
            <span>System Design & Architecture Canvas</span>
            <Layers className="w-8 h-8 text-purple-500" />
          </h1>
          <p className="text-sm text-gray-500 dark:text-gray-400 mt-1">
            Curated HLD scalability blueprints and LLD object-oriented class design templates.
          </p>
        </div>

        {/* Category Tabs */}
        <div className="flex items-center gap-1.5 bg-gray-100 dark:bg-slate-800 p-1 rounded-xl text-xs font-bold">
          {(['ALL', 'HLD', 'LLD', 'MY_DESIGNS'] as const).map((cat) => (
            <button
              key={cat}
              onClick={() => {
                setActiveCategory(cat);
                if (cat === 'MY_DESIGNS') setSelectedTemplate(null);
              }}
              className={`px-3.5 py-1.5 rounded-lg transition-all ${
                activeCategory === cat
                  ? 'bg-white dark:bg-slate-700 text-gray-900 dark:text-white shadow-xs'
                  : 'text-gray-500 dark:text-gray-400 hover:text-gray-700'
              }`}
            >
              {cat === 'ALL'
                ? 'All Templates'
                : cat === 'MY_DESIGNS'
                ? `My Saved (${myDesigns.length})`
                : cat}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <div className="py-20"><LoadingSpinner size="lg" /></div>
      ) : selectedTemplate || currentDesign ? (
        /* WORKSPACE CANVAS VIEW */
        <motion.div initial={{ opacity: 0, y: 10 }} animate={{ opacity: 1, y: 0 }} className="space-y-4">
          {/* Top Canvas Bar */}
          <div className="flex items-center justify-between p-4 bg-white dark:bg-slate-900 rounded-2xl border border-gray-200 dark:border-slate-800 shadow-sm">
            <button
              onClick={() => {
                setSelectedTemplate(null);
                setCurrentDesign(null);
              }}
              className="flex items-center gap-1.5 text-xs font-bold text-gray-500 hover:text-gray-900 dark:hover:text-white transition-colors"
            >
              <ArrowLeft className="w-4 h-4" />
              <span>Back to Catalog</span>
            </button>

            <div className="flex items-center gap-2">
              {selectedTemplate && (
                <button
                  onClick={() => setShowSolution(!showSolution)}
                  className={`px-3.5 py-1.5 rounded-xl text-xs font-bold transition-all border ${
                    showSolution
                      ? 'bg-amber-500 text-white border-amber-600 shadow-xs'
                      : 'border-gray-200 dark:border-slate-700 text-gray-700 dark:text-gray-200 hover:bg-gray-50'
                  }`}
                >
                  <Sparkles className="w-3.5 h-3.5 inline mr-1" />
                  {showSolution ? 'Hide Reference Answer' : 'View Reference Answer'}
                </button>
              )}

              <button
                onClick={handleSaveDesign}
                disabled={saving}
                className="px-4 py-1.5 rounded-xl text-xs font-bold bg-blue-600 hover:bg-blue-700 text-white shadow-md shadow-blue-500/20 flex items-center gap-1.5 active:scale-95 transition-all disabled:opacity-50"
              >
                <Save className="w-3.5 h-3.5" />
                <span>{saving ? 'Saving...' : 'Save Draft'}</span>
              </button>
            </div>
          </div>

          {/* Split Pane: Left Reference Requirements, Right User Workspace */}
          <div className="grid grid-cols-1 lg:grid-cols-2 gap-5">
            {/* Left Pane: Template Info & Blueprint */}
            <Card className="p-6 space-y-4 max-h-[750px] overflow-y-auto">
              <div>
                <div className="flex items-center gap-2">
                  <Badge>{selectedTemplate?.category || 'DESIGN'}</Badge>
                  <Badge>{selectedTemplate?.difficulty || 'INTERMEDIATE'}</Badge>
                </div>
                <h2 className="text-xl font-black text-gray-900 dark:text-white mt-2">
                  {selectedTemplate?.title || draftTitle}
                </h2>
                {selectedTemplate?.tags && (
                  <p className="text-xs text-blue-600 dark:text-blue-400 font-semibold mt-1">
                    Tags: {selectedTemplate.tags}
                  </p>
                )}
              </div>

              {selectedTemplate?.overview && (
                <div className="p-4 rounded-2xl bg-gray-50 dark:bg-slate-800/60 border border-gray-100 dark:border-slate-800 text-xs text-gray-700 dark:text-gray-300 leading-relaxed">
                  <h4 className="font-bold mb-1 text-gray-900 dark:text-white">Overview</h4>
                  {selectedTemplate.overview}
                </div>
              )}

              {selectedTemplate?.requirements && (
                <div className="p-4 rounded-2xl bg-blue-50/40 dark:bg-blue-950/20 border border-blue-100 dark:border-blue-900/40 text-xs text-gray-700 dark:text-gray-300 leading-relaxed">
                  <h4 className="font-bold mb-1.5 text-blue-700 dark:text-blue-300">Requirements Specification</h4>
                  <pre className="whitespace-pre-wrap font-sans text-xs">{selectedTemplate.requirements}</pre>
                </div>
              )}

              {selectedTemplate?.components && (
                <div className="p-4 rounded-2xl bg-purple-50/40 dark:bg-purple-950/20 border border-purple-100 dark:border-purple-900/40 text-xs text-gray-700 dark:text-gray-300 leading-relaxed">
                  <h4 className="font-bold mb-1.5 text-purple-700 dark:text-purple-300">Core Architecture Components</h4>
                  <pre className="whitespace-pre-wrap font-sans text-xs">{selectedTemplate.components}</pre>
                </div>
              )}

              {/* Reference Solution Toggle */}
              {showSolution && selectedTemplate?.sampleSolution && (
                <motion.div
                  initial={{ opacity: 0, height: 0 }}
                  animate={{ opacity: 1, height: 'auto' }}
                  className="p-4 rounded-2xl bg-amber-50 dark:bg-amber-950/30 border border-amber-200 dark:border-amber-800 text-xs text-gray-800 dark:text-gray-200 leading-relaxed space-y-2"
                >
                  <h4 className="font-bold text-amber-700 dark:text-amber-300 flex items-center gap-1.5">
                    <CheckCircle2 className="w-4 h-4" /> Official Reference Architecture & Trade-Offs
                  </h4>
                  <pre className="whitespace-pre-wrap font-sans text-xs">{selectedTemplate.sampleSolution}</pre>
                </motion.div>
              )}
            </Card>

            {/* Right Pane: User Solution Editor */}
            <Card className="p-6 space-y-4 flex flex-col justify-between max-h-[750px]">
              <div className="space-y-3">
                <div>
                  <label className="text-xs font-bold text-gray-500 uppercase tracking-wider block mb-1">
                    Design Title
                  </label>
                  <input
                    type="text"
                    value={draftTitle}
                    onChange={(e) => setDraftTitle(e.target.value)}
                    placeholder="e.g. My TinyURL Architecture Proposal"
                    className="w-full px-4 py-2 text-sm font-bold rounded-xl border border-gray-200 dark:border-slate-700 bg-white dark:bg-slate-900 text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-blue-500"
                  />
                </div>

                <div>
                  <div className="flex items-center justify-between mb-1">
                    <label className="text-xs font-bold text-gray-500 uppercase tracking-wider block">
                      Architecture Draft & Deep Dives
                    </label>
                    <span className="text-[10px] text-gray-400">Markdown syntax</span>
                  </div>
                  <textarea
                    value={draftContent}
                    onChange={(e) => setDraftContent(e.target.value)}
                    placeholder="Write your component breakdown, API endpoints, schema designs, cache strategies, and trade-offs..."
                    className="w-full h-80 p-4 rounded-2xl border border-gray-200 dark:border-slate-700 bg-gray-50 dark:bg-slate-900 font-mono text-xs text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-blue-500 resize-none leading-relaxed"
                  />
                </div>

                <div>
                  <label className="text-xs font-bold text-gray-500 uppercase tracking-wider block mb-1">
                    Diagram Structure (Mermaid / Graph notation)
                  </label>
                  <textarea
                    value={draftDiagram}
                    onChange={(e) => setDraftDiagram(e.target.value)}
                    placeholder="graph TD\n  Client --> LB[Load Balancer]\n  LB --> API"
                    className="w-full h-28 p-3 rounded-xl border border-gray-200 dark:border-slate-700 bg-gray-50 dark:bg-slate-900 font-mono text-xs text-gray-900 dark:text-white outline-none focus:ring-2 focus:ring-blue-500 resize-none"
                  />
                </div>
              </div>

              <div className="pt-3 border-t border-gray-100 dark:border-slate-800 flex items-center justify-between">
                <span className="text-[11px] text-gray-400">
                  {currentDesign?.updatedAt ? `Last saved: ${new Date(currentDesign.updatedAt).toLocaleTimeString()}` : 'Not saved yet'}
                </span>
                <button
                  onClick={handleSaveDesign}
                  disabled={saving}
                  className="px-5 py-2 rounded-xl text-xs font-bold bg-blue-600 hover:bg-blue-700 text-white shadow-md shadow-blue-500/20 active:scale-95 transition-all"
                >
                  {saving ? 'Saving...' : 'Save Draft ✓'}
                </button>
              </div>
            </Card>
          </div>
        </motion.div>
      ) : activeCategory === 'MY_DESIGNS' ? (
        /* MY DESIGNS TAB */
        <div className="space-y-4">
          {myDesigns.length === 0 ? (
            <Card className="p-12 text-center text-gray-500 dark:text-gray-400">
              <Layers className="w-10 h-10 mx-auto mb-2 text-gray-400 opacity-50" />
              <p className="text-sm font-bold">No saved system designs yet.</p>
              <p className="text-xs mt-1">Select a blueprint from the catalog to draft and save your architecture!</p>
            </Card>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {myDesigns.map((d) => (
                <Card key={d.id} className="p-5 space-y-3 hover:border-blue-300 transition-colors">
                  <div className="flex items-center justify-between">
                    <h3 className="text-base font-bold text-gray-900 dark:text-white">
                      {d.title}
                    </h3>
                    <span className="text-[11px] text-gray-400">
                      {new Date(d.updatedAt).toLocaleDateString()}
                    </span>
                  </div>

                  {d.templateTitle && (
                    <span className="inline-block text-[11px] font-bold text-blue-600 dark:text-blue-400 bg-blue-50 dark:bg-blue-950/40 px-2 py-0.5 rounded-md">
                      Template: {d.templateTitle}
                    </span>
                  )}

                  <p className="text-xs text-gray-500 dark:text-gray-400 line-clamp-2">
                    {d.content || 'No content written yet.'}
                  </p>

                  <div className="pt-2 border-t border-gray-100 dark:border-slate-800 flex justify-end">
                    <button
                      onClick={() => handleSelectUserDesign(d)}
                      className="text-xs font-bold text-blue-600 dark:text-blue-400 hover:underline flex items-center gap-1"
                    >
                      <span>Open in Canvas</span>
                      <ChevronRight className="w-3.5 h-3.5" />
                    </button>
                  </div>
                </Card>
              ))}
            </div>
          )}
        </div>
      ) : (
        /* TEMPLATE CATALOG VIEW */
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {filteredTemplates.map((template, idx) => (
            <motion.div
              key={template.id}
              initial={{ opacity: 0, y: 10 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.25, delay: idx * 0.04 }}
              whileHover={{ y: -3 }}
            >
              <Card
                className="p-6 h-full flex flex-col justify-between cursor-pointer hover:border-purple-300 dark:hover:border-purple-800 transition-all shadow-sm group"
                onClick={() => handleSelectTemplate(template)}
              >
                <div className="space-y-3">
                  <div className="flex items-center justify-between">
                    <span className={`text-[10px] font-black uppercase px-2 py-0.5 rounded-md ${
                      template.category === 'HLD'
                        ? 'bg-purple-100 dark:bg-purple-950/60 text-purple-700 dark:text-purple-300'
                        : 'bg-blue-100 dark:bg-blue-950/60 text-blue-700 dark:text-blue-300'
                    }`}>
                      {template.category}
                    </span>
                    <Badge variant={template.difficulty.toLowerCase() as any}>{template.difficulty}</Badge>
                  </div>

                  <h3 className="text-base font-bold text-gray-900 dark:text-white group-hover:text-blue-600 dark:group-hover:text-blue-400 transition-colors">
                    {template.title}
                  </h3>

                  <p className="text-xs text-gray-500 dark:text-gray-400 line-clamp-2 leading-relaxed">
                    {template.overview}
                  </p>

                  <div className="text-[11px] font-semibold text-gray-400 truncate">
                    {template.tags}
                  </div>
                </div>

                <div className="pt-4 mt-3 border-t border-gray-100 dark:border-slate-800/80 flex items-center justify-between text-xs font-bold text-blue-600 dark:text-blue-400">
                  <span>Open Architecture Blueprint</span>
                  <ChevronRight className="w-4 h-4 group-hover:translate-x-1 transition-transform" />
                </div>
              </Card>
            </motion.div>
          ))}
        </div>
      )}
    </div>
  );
};

export default DesignCanvasPage;
